package com.smarttask.app.contextmatching.db;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "task_context_matches",
        indices = {
                @Index(value = {"taskId", "snapshotId"}),
                @Index(value = {"taskId", "matchedAt"}),
                @Index("cooldownUntil")
        }
)
public class TaskContextMatch {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(index = true)
    public long taskId;

    @ColumnInfo(index = true)
    public long snapshotId;

    public long matchedAt;

    public float relevanceScore;

    public String matchReasons;

    public boolean shouldTriggerNow;

    @Nullable
    public Long cooldownUntil;

    public String triggerType;

    @Nullable
    public String blockedBy;
}
