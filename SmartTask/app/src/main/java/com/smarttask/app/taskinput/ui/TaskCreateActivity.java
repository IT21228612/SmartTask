package com.smarttask.app.taskinput.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskCreateActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private TaskDao taskDao;
    private EditText titleInput;
    private EditText descriptionInput;
    private TextView dueDateDisplay;
    private Spinner prioritySpinner;
    private Long selectedDueDate;
    private long editingTaskId = -1L;
    private long createdAt = -1L;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        taskDao = TaskDatabase.getInstance(this).taskDao();

        titleInput = findViewById(R.id.task_title_input);
        descriptionInput = findViewById(R.id.task_description_input);
        dueDateDisplay = findViewById(R.id.task_due_date_display);
        prioritySpinner = findViewById(R.id.task_priority_spinner);
        Button dueDateButton = findViewById(R.id.task_due_date_button);
        Button clearDueDateButton = findViewById(R.id.task_clear_due_date_button);
        Button locationButton = findViewById(R.id.task_location_button);
        Button saveButton = findViewById(R.id.save_task_button);
        Button cancelButton = findViewById(R.id.cancel_task_button);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_priority_options,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        dueDateButton.setOnClickListener(v -> openDueDatePicker());
        clearDueDateButton.setOnClickListener(v -> {
            selectedDueDate = null;
            updateDueDateDisplay();
        });
        locationButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.task_location_placeholder_toast, Toast.LENGTH_SHORT).show());
        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> finish());

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId != -1L) {
            loadTask(taskId);
        } else {
            createdAt = System.currentTimeMillis();
        }
    }

    private void loadTask(long taskId) {
        Task task = taskDao.getTaskById(taskId);
        if (task == null) {
            Toast.makeText(this, R.string.task_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editingTaskId = taskId;
        createdAt = task.getCreatedAt();
        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());
        selectedDueDate = task.getDueAt();
        updateDueDateDisplay();
        prioritySpinner.setSelection(Math.max(0, Math.min(task.getPriority(), 2)));
    }

    private void openDueDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        if (selectedDueDate != null) {
            calendar.setTimeInMillis(selectedDueDate);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    openTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void openTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = calendar.getTimeInMillis();
                    updateDueDateDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDueDateDisplay() {
        if (selectedDueDate == null) {
            dueDateDisplay.setText(R.string.task_due_date_not_set);
        } else {
            dueDateDisplay.setText(dateFormat.format(selectedDueDate));
        }
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError(getString(R.string.task_title_required));
            return;
        }

        if (createdAt == -1L) {
            createdAt = System.currentTimeMillis();
        }

        int priority = prioritySpinner.getSelectedItemPosition();

        Task task = new Task();
        task.setId(editingTaskId);
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatedAt(createdAt);
        task.setDueAt(selectedDueDate);
        task.setPriority(priority);
        task.setLocationLat(null);
        task.setLocationLng(null);
        task.setLocationRadius(null);

        if (editingTaskId == -1L) {
            long newId = taskDao.insertTask(task);
            task.setId(newId);
        } else {
            taskDao.updateTask(task);
        }

        Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK);
        finish();
    }
}
