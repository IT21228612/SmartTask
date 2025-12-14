package com.smarttask.app.taskinput.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    @Nullable
    private String description;
    private long createdAt;
    @Nullable
    private Long dueAt;
    private int priority;
    @Nullable
    private Double locationLat;
    @Nullable
    private Double locationLng;
    @Nullable
    private Float locationRadius;

    public Task() {
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

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Nullable
    public Long getDueAt() {
        return dueAt;
    }

    public void setDueAt(@Nullable Long dueAt) {
        this.dueAt = dueAt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Nullable
    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(@Nullable Double locationLat) {
        this.locationLat = locationLat;
    }

    @Nullable
    public Double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(@Nullable Double locationLng) {
        this.locationLng = locationLng;
    }

    @Nullable
    public Float getLocationRadius() {
        return locationRadius;
    }

    public void setLocationRadius(@Nullable Float locationRadius) {
        this.locationRadius = locationRadius;
    }
}
