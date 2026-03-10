package com.smarttask.app.contextmatching.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskContextMatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TaskContextMatch> matches);

    @Query("SELECT * FROM task_context_matches WHERE taskId = :taskId ORDER BY matchedAt DESC LIMIT 1")
    TaskContextMatch getLatestForTask(long taskId);

    @Query("SELECT COUNT(*) FROM task_context_matches WHERE taskId = :taskId AND shouldTriggerNow = 1 AND triggerType = 'NOTIFICATION'")
    int getNotificationTriggerCountForTask(long taskId);



    @Query("SELECT * FROM task_context_matches WHERE snapshotId = :snapshotId")
    List<TaskContextMatch> getBySnapshotId(long snapshotId);

    @Query("DELETE FROM task_context_matches WHERE matchedAt < :cutoff")
    void deleteOlderThan(long cutoff);
}
