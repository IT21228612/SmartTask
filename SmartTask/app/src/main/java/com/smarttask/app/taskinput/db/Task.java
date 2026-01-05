package com.smarttask.app.taskinput.db;

import androidx.annotation.Nullable;
import androidx.room.*;


@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "title")
    private String title;
    @Nullable
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "category")
    private String category;
    @ColumnInfo(name = "createdAt", defaultValue = "0")
    private long createdAt;
    @ColumnInfo(name = "updatedAt", defaultValue = "0")
    private long updatedAt;
    @Nullable
    @ColumnInfo(name = "dueAt")
    private Long dueAt;
    @ColumnInfo(name = "priority", defaultValue = "0")
    private int priority;
    @Nullable
    @ColumnInfo(name = "locationLat")
    private Double locationLat;
    @Nullable
    @ColumnInfo(name = "locationLng")
    private Double locationLng;
    @Nullable
    @ColumnInfo(name = "locationRadiusM")
    private Float locationRadius;
    @Nullable
    @ColumnInfo(name = "locationLabel")
    private String locationLabel;
    @Nullable
    @ColumnInfo(name = "estimatedDurationMin")
    private Integer estimatedDurationMin;
    @Nullable
    @ColumnInfo(name = "preferredStartTime")
    private Long preferredStartTime;
    @Nullable
    @ColumnInfo(name = "preferredEndTime")
    private Long preferredEndTime;
    @ColumnInfo(name = "notificationsEnabled", defaultValue = "1")
    private boolean notificationsEnabled;
    @ColumnInfo(name = "completed", defaultValue = "0")
    private boolean completed;
    @Nullable
    @ColumnInfo(name = "completedAt")
    private Long completedAt;
    @ColumnInfo(name = "archived", defaultValue = "0")
    private boolean archived;
    @Nullable
    @ColumnInfo(name = "snoozeUntil")
    private Long snoozeUntil;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
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

    @Nullable
    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(@Nullable String locationLabel) {
        this.locationLabel = locationLabel;
    }

    @Nullable
    public Integer getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(@Nullable Integer estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    @Nullable
    public Long getPreferredStartTime() {
        return preferredStartTime;
    }

    public void setPreferredStartTime(@Nullable Long preferredStartTime) {
        this.preferredStartTime = preferredStartTime;
    }

    @Nullable
    public Long getPreferredEndTime() {
        return preferredEndTime;
    }

    public void setPreferredEndTime(@Nullable Long preferredEndTime) {
        this.preferredEndTime = preferredEndTime;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Nullable
    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(@Nullable Long completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Nullable
    public Long getSnoozeUntil() {
        return snoozeUntil;
    }

    public void setSnoozeUntil(@Nullable Long snoozeUntil) {
        this.snoozeUntil = snoozeUntil;
    }
}
