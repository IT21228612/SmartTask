package com.smarttask.app.taskinput.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTaskDao implements TaskDao {
    private static final InMemoryTaskDao INSTANCE = new InMemoryTaskDao();

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private InMemoryTaskDao() {
    }

    public static InMemoryTaskDao getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized Task insert(Task task) {
        long id = idGenerator.getAndIncrement();
        Task stored = new Task(id, task.getTitle(), task.getDescription(), task.getDueDate(), task.getLocation(), task.getPriority());
        tasks.add(stored);
        return new Task(stored);
    }

    @Override
    public synchronized void update(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == task.getId()) {
                tasks.set(i, new Task(task));
                return;
            }
        }
    }

    @Override
    public synchronized void delete(long taskId) {
        tasks.removeIf(task -> task.getId() == taskId);
    }

    @Override
    public synchronized List<Task> getTasks() {
        List<Task> copy = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            copy.add(new Task(task));
        }
        return copy;
    }

    @Override
    public synchronized Task getTaskById(long taskId) {
        for (Task task : tasks) {
            if (task.getId() == taskId) {
                return new Task(task);
            }
        }
        return null;
    }
}
