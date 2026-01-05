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

    @Query("SELECT * FROM tasks ORDER BY completed ASC, updatedAt DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC")
    List<Task> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskById(long taskId);
}
