package com.smarttask.app.prioritization.logic;

import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;

import java.util.ArrayList;
import java.util.List;

public class DisplayOrderUpdater {

    public void update(TaskDao taskDao, List<PrioritizedTask> prioritizedTasks) {
        List<Task> updates = new ArrayList<>();
        for (int i = 0; i < prioritizedTasks.size(); i++) {
            Task task = prioritizedTasks.get(i).task;
            task.setDisplayOrder(i + 1L);
            updates.add(task);
        }

        if (!updates.isEmpty()) {
            taskDao.updateTasks(updates);
        }
    }
}
