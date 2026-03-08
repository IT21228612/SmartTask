package com.smarttask.app.taskinput.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.smarttask.app.R;
import com.smarttask.app.contextacquisition.api.ContextEngine;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;
import com.smarttask.app.voiceCommandTaskCreation.OpenAiVoiceTaskParser;
import com.smarttask.app.voiceCommandTaskCreation.ParsedVoiceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.TaskClickListener {

    private TaskDao taskDao;
    private TaskAdapter adapter;
    private TextView emptyView;
    private boolean isDragging = false;
    private final ExecutorService voiceExecutor = Executors.newSingleThreadExecutor();
    private final OpenAiVoiceTaskParser voiceTaskParser = new OpenAiVoiceTaskParser();

    private final ActivityResultLauncher<Intent> taskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshTasks()
    );

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    requestBackgroundLocationPermissionIfNeeded();
                }
            }
    );

    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                // No-op: periodic snapshots already handle denied background permission with NO_LOCATION.
            }
    );

    private final ActivityResultLauncher<String> recordAudioPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    launchSpeechRecognizer();
                } else {
                    Toast.makeText(this, R.string.voice_permission_required, Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> voiceRecognizerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                ArrayList<String> textResults = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (textResults == null || textResults.isEmpty()) {
                    return;
                }
                handleVoiceTranscript(textResults.get(0));
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        requestLocationPermissionsForBackgroundSnapshots();

        taskDao = TaskDatabase.getInstance(this).taskDao();

        RecyclerView recyclerView = findViewById(R.id.task_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fab = findViewById(R.id.add_task_button);
        FloatingActionButton voiceFab = findViewById(R.id.voice_task_button);
        MaterialToolbar toolbar = findViewById(R.id.task_list_toolbar);

        toolbar.setNavigationOnClickListener(this::showTopMenu);
        toolbar.setOnMenuItemClickListener(this::handleToolbarItem);

        adapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeToActionCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return adapter.moveTask(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = adapter.getTaskAt(position);
                if (task == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }

                if (direction == ItemTouchHelper.LEFT) {
                    showDeleteConfirmation(task, position);
                    return;
                }

                onTaskClicked(task);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    isDragging = true;
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (isDragging) {
                    persistTaskOrder();
                    isDragging = false;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX,
                                    float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    int itemHeight = itemView.getBottom() - itemView.getTop();

                    boolean isSwipeRight = dX > 0;
                    int iconRes = isSwipeRight ? R.drawable.ic_swipe_edit : R.drawable.ic_swipe_delete;
                    int backgroundColor = isSwipeRight
                            ? ContextCompat.getColor(TaskListActivity.this, R.color.swipe_edit_background)
                            : ContextCompat.getColor(TaskListActivity.this, R.color.swipe_delete_background);

                    Drawable icon = ContextCompat.getDrawable(TaskListActivity.this, iconRes);
                    ColorDrawable background = new ColorDrawable(backgroundColor);

                    if (dX != 0) {
                        if (isSwipeRight) {
                            background.setBounds(itemView.getLeft(), itemView.getTop(),
                                    itemView.getLeft() + Math.round(dX), itemView.getBottom());
                        } else {
                            background.setBounds(itemView.getRight() + Math.round(dX), itemView.getTop(),
                                    itemView.getRight(), itemView.getBottom());
                        }
                        background.draw(canvas);
                    }

                    if (icon != null) {
                        int iconHeight = icon.getIntrinsicHeight();
                        int iconWidth = icon.getIntrinsicWidth();
                        int iconTop = itemView.getTop() + (itemHeight - iconHeight) / 2;
                        int iconMargin = (itemHeight - iconHeight) / 2;

                        if (isSwipeRight) {
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + iconWidth;
                            icon.setBounds(iconLeft, iconTop, iconRight, iconTop + iconHeight);
                        } else {
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - iconWidth;
                            icon.setBounds(iconLeft, iconTop, iconRight, iconTop + iconHeight);
                        }
                        icon.draw(canvas);
                    }
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeToActionCallback).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(v -> taskLauncher.launch(new Intent(this, TaskCreateActivity.class)));
        voiceFab.setOnClickListener(v -> startVoiceTaskFlow());

        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void requestLocationPermissionsForBackgroundSnapshots() {
        if (!hasForegroundLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        requestBackgroundLocationPermissionIfNeeded();
    }

    private void requestBackgroundLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    private boolean hasForegroundLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void startVoiceTaskFlow() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        launchSpeechRecognizer();
    }

    private void launchSpeechRecognizer() {
        try {
            Toast.makeText(this, R.string.voice_listening, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_command_task));
            voiceRecognizerLauncher.launch(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.voice_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleVoiceTranscript(String transcript) {
        Toast.makeText(this, R.string.voice_processing, Toast.LENGTH_SHORT).show();
        voiceExecutor.execute(() -> {
            ParsedVoiceTask parsed = voiceTaskParser.parseOrFallback(this, transcript);
            int estimatedDuration = computeEstimatedDurationMinutes(parsed);
            runOnUiThread(() -> {
                Intent intent = new Intent(this, TaskCreateActivity.class);
                intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_TITLE,
                        parsed.getTaskTitle() == null ? "Voice task" : parsed.getTaskTitle());
                intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_DESCRIPTION,
                        parsed.getDescription() == null ? transcript : parsed.getDescription());
                if (parsed.getCategory() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_CATEGORY, parsed.getCategory());
                }
                if (parsed.getPriority() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_PRIORITY, parsed.getPriority());
                }
                if (parsed.getDueDatetime() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_DUE_AT, parsed.getDueDatetime());
                }
                if (parsed.getPreferredStartDatetime() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_PREFERRED_START, parsed.getPreferredStartDatetime());
                }
                if (parsed.getPreferredEndDatetime() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_PREFERRED_END, parsed.getPreferredEndDatetime());
                }
                if (estimatedDuration >= 0) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_ESTIMATED_DURATION_MIN, estimatedDuration);
                }
                if (parsed.getLocationRadiusMeters() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_LOCATION_RADIUS, parsed.getLocationRadiusMeters());
                }
                if (parsed.getEnableNotifications() != null) {
                    intent.putExtra(TaskCreateActivity.EXTRA_PREFILL_NOTIFICATIONS, parsed.getEnableNotifications());
                }
                if ("Voice task".equals(intent.getStringExtra(TaskCreateActivity.EXTRA_PREFILL_TITLE))) {
                    Toast.makeText(this, R.string.voice_fallback_used, Toast.LENGTH_SHORT).show();
                }
                taskLauncher.launch(intent);
            });
        });
    }

    private int computeEstimatedDurationMinutes(ParsedVoiceTask parsedVoiceTask) {
        Long start = parseIsoTime(parsedVoiceTask.getPreferredStartDatetime());
        Long end = parseIsoTime(parsedVoiceTask.getPreferredEndDatetime());
        if (start == null || end == null || end <= start) {
            return -1;
        }
        return (int) TimeUnit.MILLISECONDS.toMinutes(end - start);
    }

    @Nullable
    private Long parseIsoTime(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.Instant.parse(value).toEpochMilli();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void refreshTasks() {
        List<Task> tasks = taskDao.getAllTasks();
        if (ensureDisplayOrder(tasks)) {
            tasks = taskDao.getAllTasks();
        }
        adapter.submitList(tasks);
        emptyView.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean ensureDisplayOrder(List<Task> tasks) {
        boolean hasMissingOrder = false;
        for (Task task : tasks) {
            if (task.getDisplayOrder() <= 0) {
                hasMissingOrder = true;
                break;
            }
        }
        if (!hasMissingOrder) {
            return false;
        }
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            long desiredOrder = i + 1L;
            if (task.getDisplayOrder() != desiredOrder) {
                task.setDisplayOrder(desiredOrder);
                taskDao.updateTask(task);
            }
        }
        return true;
    }

    private void persistTaskOrder() {
        List<Task> currentTasks = adapter.getCurrentTasks();
        for (int i = 0; i < currentTasks.size(); i++) {
            Task task = currentTasks.get(i);
            long desiredOrder = i + 1L;
            if (task.getDisplayOrder() != desiredOrder) {
                task.setDisplayOrder(desiredOrder);
                taskDao.updateTask(task);
            }
        }
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(this, TaskCreateActivity.class);
        intent.putExtra(TaskCreateActivity.EXTRA_TASK_ID, task.getId());
        taskLauncher.launch(intent);
    }

    @Override
    public void onTaskLongClicked(Task task) {
        // Intentionally no-op: long-press should not trigger any action.
    }

    @Override
    public void onTaskCompletionToggled(Task task, boolean isCompleted) {
        task.setCompleted(isCompleted);
        long now = System.currentTimeMillis();
        task.setUpdatedAt(now);
        task.setCompletedAt(isCompleted ? now : null);
        if (isCompleted) {
            Long maxDisplayOrder = taskDao.getMaxDisplayOrder();
            task.setDisplayOrder(maxDisplayOrder == null ? 1L : maxDisplayOrder + 1L);
        } else {
            Long minDisplayOrder = taskDao.getMinDisplayOrder();
            task.setDisplayOrder(minDisplayOrder == null ? 1L : minDisplayOrder - 1L);
        }
        taskDao.updateTask(task);
        refreshTasks();
    }

    private void showDeleteConfirmation(Task task, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    taskDao.deleteTask(task);
                    ContextEngine.getInstance(this).syncTaskGeofences();
                    refreshTasks();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    private void showTopMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.START | Gravity.TOP);
        popupMenu.getMenuInflater().inflate(R.menu.menu_task_list_nav, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_item_debug) {
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.menu_item_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return true;
        });
        popupMenu.show();
    }

    private boolean handleToolbarItem(MenuItem item) {
        if (item.getItemId() == R.id.action_user) {
            return true;
        }
        return false;
    }
}
