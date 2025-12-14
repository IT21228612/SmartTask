package com.smarttask.app.taskinput.db;

import java.util.List;

// taskinput/db/TaskDao.java

public interface TaskDao {
    Task insert(Task task);

    void update(Task task);

    void delete(long taskId);

    List<Task> getTasks();

    Task getTaskById(long taskId);

    static TaskDao inMemory() {
        return InMemoryTaskDao.getInstance();
    }
}
