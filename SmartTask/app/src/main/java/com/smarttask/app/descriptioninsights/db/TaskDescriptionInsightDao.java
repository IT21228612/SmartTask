package com.smarttask.app.descriptioninsights.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDescriptionInsightDao {

    @Query("SELECT * FROM task_description_insights WHERE taskId = :taskId LIMIT 1")
    TaskDescriptionInsight getByTaskId(long taskId);

    @Query("SELECT * FROM task_description_insights WHERE taskId IN (:taskIds)")
    List<TaskDescriptionInsight> getByTaskIds(List<Long> taskIds);

    @Insert
    long insert(TaskDescriptionInsight insight);

    @Update
    void update(TaskDescriptionInsight insight);
}
