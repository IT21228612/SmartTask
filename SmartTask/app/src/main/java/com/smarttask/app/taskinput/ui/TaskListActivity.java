package com.smarttask.app.taskinput.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.List;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.TaskClickListener {

    private TaskDao taskDao;
    private TaskAdapter adapter;
    private TextView emptyView;

    private final ActivityResultLauncher<Intent> taskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshTasks()
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        taskDao = TaskDatabase.getInstance(this).taskDao();

        RecyclerView recyclerView = findViewById(R.id.task_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fab = findViewById(R.id.add_task_button);

        adapter = new TaskAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = adapter.getTaskAt(position);
                if (task != null) {
                    showDeleteConfirmation(task, position);
                }
            }
        };

        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(v -> taskLauncher.launch(new Intent(this, TaskCreateActivity.class)));

        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void refreshTasks() {
        List<Task> tasks = taskDao.getAllTasks();
        adapter.submitList(tasks);
        emptyView.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(this, TaskCreateActivity.class);
        intent.putExtra(TaskCreateActivity.EXTRA_TASK_ID, task.getId());
        taskLauncher.launch(intent);
    }

    @Override
    public void onTaskLongClicked(Task task) {
        taskDao.deleteTask(task);
        refreshTasks();
    }

    @Override
    public void onTaskCompletionToggled(Task task, boolean isCompleted) {
        task.setCompleted(isCompleted);
        long now = System.currentTimeMillis();
        task.setUpdatedAt(now);
        task.setCompletedAt(isCompleted ? now : null);
        taskDao.updateTask(task);
        refreshTasks();
    }

    private void showDeleteConfirmation(Task task, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    taskDao.deleteTask(task);
                    refreshTasks();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }
}
