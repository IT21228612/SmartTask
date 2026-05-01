package com.smarttask.app.taskinput.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UsageStatDao {

    @Insert
    long insert(UsageStatEvent event);

    @Query("SELECT COUNT(*) FROM usage_stat_events WHERE eventType = :eventType")
    int countByType(String eventType);

    @Query("SELECT EXISTS(SELECT 1 FROM usage_stat_events WHERE eventType = :eventType AND notificationId = :notificationId)")
    boolean hasEventForNotification(String eventType, int notificationId);
}
