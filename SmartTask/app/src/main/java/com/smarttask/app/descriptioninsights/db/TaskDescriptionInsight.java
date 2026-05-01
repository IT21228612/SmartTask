package com.smarttask.app.descriptioninsights.db;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_description_insights", indices = {@Index(value = {"taskId"}, unique = true)})
public class TaskDescriptionInsight {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "taskId")
    public long taskId;

    @ColumnInfo(name = "descriptionHash")
    public String descriptionHash;

    @ColumnInfo(name = "riskIfDelayed", defaultValue = "0")
    public float riskIfDelayed;

    @ColumnInfo(name = "requiresDeepFocus", defaultValue = "0")
    public boolean requiresDeepFocus;

    @Nullable
    @ColumnInfo(name = "suggestedDurationMin")
    public Integer suggestedDurationMin;

    @ColumnInfo(name = "complexityLevel", defaultValue = "0")
    public int complexityLevel;

    @ColumnInfo(name = "energyDemand", defaultValue = "0")
    public int energyDemand;

    @ColumnInfo(name = "relationshipValue", defaultValue = "0")
    public int relationshipValue;

    @Nullable
    @ColumnInfo(name = "rawJson")
    public String rawJson;

    @Nullable
    @ColumnInfo(name = "model")
    public String model;

    @ColumnInfo(name = "updatedAt")
    public long updatedAt;
}
