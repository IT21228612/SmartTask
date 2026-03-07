package com.smarttask.app.taskinput.db;
import androidx.room.*;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks ORDER BY completed ASC, displayOrder ASC, updatedAt DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    List<Task> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskById(long taskId);

    @Query("SELECT * FROM tasks WHERE completed = 0 AND archived = 0 " +
            "AND (snoozeUntil IS NULL OR snoozeUntil <= :now) " +
            "AND notificationsEnabled = 1")
    List<Task> getActiveTasksForMatching(long now);

    @Query("SELECT * FROM tasks WHERE completed = 0 AND archived = 0 ORDER BY displayOrder ASC, updatedAt DESC")
    List<Task> getActiveIncompleteTasksForPrioritization();

    @Query("SELECT * FROM tasks WHERE completed = 0 AND archived = 0 " +
            "AND (snoozeUntil IS NULL OR snoozeUntil <= :now) " +
            "AND notificationsEnabled = 1 " +
            "AND locationLat IS NOT NULL AND locationLng IS NOT NULL " +
            "AND locationLat BETWEEN :minLat AND :maxLat " +
            "AND locationLng BETWEEN :minLng AND :maxLng")
    List<Task> getActiveTasksInBoundingBox(long now, double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT MAX(displayOrder) FROM tasks")
    Long getMaxDisplayOrder();

    @Query("SELECT MIN(displayOrder) FROM tasks")
    Long getMinDisplayOrder();

    @Update
    void updateTasks(List<Task> tasks);
}
