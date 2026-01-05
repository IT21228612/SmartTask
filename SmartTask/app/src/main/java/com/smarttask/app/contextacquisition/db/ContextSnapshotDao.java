package com.smarttask.app.contextacquisition.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContextSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContextSnapshot snapshot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContextSnapshot> snapshots);

    @Query("SELECT * FROM context_snapshots ORDER BY timestamp DESC LIMIT 1")
    ContextSnapshot getLatest();

    @Query("SELECT * FROM context_snapshots WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    List<ContextSnapshot> getBetween(long start, long end);

    @Query("DELETE FROM context_snapshots WHERE timestamp < :cutoff")
    void deleteOlderThan(long cutoff);
}
