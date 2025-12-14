// taskinput/ui/TaskListActivity.java
package com.smarttask.app.taskinput.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private final TaskDao taskDao = TaskDao.inMemory();
    private ArrayAdapter<Task> taskAdapter;

    private final ActivityResultLauncher<Intent> createTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshTasks();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        ListView taskList = findViewById(R.id.task_list);
        FloatingActionButton addTaskButton = findViewById(R.id.add_task_button);

        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        taskList.setAdapter(taskAdapter);
        taskList.setEmptyView(findViewById(R.id.empty_view));

        taskList.setOnItemClickListener((parent, view, position, id) -> {
            Task selected = taskAdapter.getItem(position);
            if (selected != null) {
                Intent editIntent = new Intent(this, TaskCreateActivity.class);
                editIntent.putExtra(TaskCreateActivity.EXTRA_TASK_ID, selected.getId());
                createTaskLauncher.launch(editIntent);
            }
        });

        taskList.setOnItemLongClickListener((parent, view, position, id) -> {
            Task selected = taskAdapter.getItem(position);
            if (selected != null) {
                taskDao.delete(selected.getId());
                Toast.makeText(this, getString(R.string.task_deleted, selected.getTitle()), Toast.LENGTH_SHORT).show();
                refreshTasks();
            }
            return true;
        });

        addTaskButton.setOnClickListener(v -> createTaskLauncher.launch(new Intent(this, TaskCreateActivity.class)));
        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void refreshTasks() {
        List<Task> tasks = taskDao.getTasks();
        taskAdapter.clear();
        taskAdapter.addAll(tasks);
        taskAdapter.notifyDataSetChanged();
    }
}
