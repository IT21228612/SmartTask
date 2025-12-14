package com.smarttask.app.taskinput.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

// taskinput/db/Task.java

public class Task {
    private long id;
    private String title;
    private String description;
    private long dueDate;
    private String location;
    private int priority;

    public Task() {
    }

    public Task(long id, String title, String description, long dueDate, String location, int priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.location = location;
        this.priority = priority;
    }

    public Task(Task other) {
        this(other.id, other.title, other.description, other.dueDate, other.location, other.priority);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(title == null ? "" : title);
        if (dueDate > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
            builder.append(" · due ").append(formatter.format(new Date(dueDate)));
        }
        if (priority != 0) {
            builder.append(" · priority ").append(priority);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task)) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
