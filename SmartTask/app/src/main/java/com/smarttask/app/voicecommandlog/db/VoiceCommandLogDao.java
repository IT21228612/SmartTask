package com.smarttask.app.voicecommandlog.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VoiceCommandLogDao {

    @Insert
    long insert(VoiceCommandLog voiceCommandLog);

    @Query("SELECT * FROM voice_command_logs ORDER BY id DESC")
    List<VoiceCommandLog> getAll();
}
