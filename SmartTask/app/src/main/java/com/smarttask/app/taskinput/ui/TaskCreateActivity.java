// taskinput/ui/TaskCreateActivity.java
package com.smarttask.app.taskinput.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskCreateActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private final TaskDao taskDao = TaskDao.inMemory();

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dueDateInput;
    private EditText locationInput;
    private EditText priorityInput;
    private long editingTaskId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        titleInput = findViewById(R.id.task_title_input);
        descriptionInput = findViewById(R.id.task_description_input);
        dueDateInput = findViewById(R.id.task_due_date_input);
        locationInput = findViewById(R.id.task_location_input);
        priorityInput = findViewById(R.id.task_priority_input);
        Button saveButton = findViewById(R.id.save_task_button);

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId != -1L) {
            Task existing = taskDao.getTaskById(taskId);
            if (existing != null) {
                editingTaskId = taskId;
                titleInput.setText(existing.getTitle());
                descriptionInput.setText(existing.getDescription());
                if (existing.getDueDate() > 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    dueDateInput.setText(formatter.format(new Date(existing.getDueDate())));
                }
                locationInput.setText(existing.getLocation());
                priorityInput.setText(String.valueOf(existing.getPriority()));
            }
        }

        saveButton.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDateText = dueDateInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String priorityText = priorityInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError(getString(R.string.task_title_required));
            return;
        }

        int priority = TextUtils.isEmpty(priorityText) ? 0 : Integer.parseInt(priorityText);
        long dueDate = parseDueDate(dueDateText);

        Task task = new Task(editingTaskId, title, description, dueDate, location, priority);
        if (editingTaskId == -1L) {
            taskDao.insert(task);
        } else {
            taskDao.update(task);
        }

        Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private long parseDueDate(String dueDateText) {
        if (TextUtils.isEmpty(dueDateText)) {
            return 0L;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date parsed = formatter.parse(dueDateText);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (ParseException e) {
            Toast.makeText(this, R.string.task_due_date_format_hint, Toast.LENGTH_SHORT).show();
            return 0L;
        }
    }
}
