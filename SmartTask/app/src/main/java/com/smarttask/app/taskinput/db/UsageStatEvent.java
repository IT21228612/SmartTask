package com.smarttask.app.taskinput.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "usage_stat_events",
        indices = {
                @Index(value = {"eventType", "createdAt"}),
                @Index(value = {"taskId"}),
                @Index(value = {"notificationId", "eventType"})
        }
)
public class UsageStatEvent {

    public static final String EVENT_TASK_CREATED = "TASK_CREATED";
    public static final String EVENT_TASK_COMPLETED = "TASK_COMPLETED";
    public static final String EVENT_NOTIFICATION_SENT = "NOTIFICATION_SENT";
    public static final String EVENT_NOTIFICATION_IGNORED = "NOTIFICATION_IGNORED";
    public static final String EVENT_NOTIFICATION_ACTIONED = "NOTIFICATION_ACTIONED";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "eventType")
    public String eventType;

    @Nullable
    @ColumnInfo(name = "taskId")
    public Long taskId;

    @Nullable
    @ColumnInfo(name = "notificationId")
    public Integer notificationId;

    @ColumnInfo(name = "createdAt")
    public long createdAt;

    public UsageStatEvent(@NonNull String eventType, @Nullable Long taskId, @Nullable Integer notificationId, long createdAt) {
        this.eventType = eventType;
        this.taskId = taskId;
        this.notificationId = notificationId;
        this.createdAt = createdAt;
    }
}
