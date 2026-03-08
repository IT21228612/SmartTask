package com.smarttask.app.voicecommandlog.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "voice_command_logs")
public class VoiceCommandLog {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "transcript")
    private String transcript;

    @ColumnInfo(name = "extractedJson")
    private String extractedJson;

    @ColumnInfo(name = "createdAt")
    private long createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getExtractedJson() {
        return extractedJson;
    }

    public void setExtractedJson(String extractedJson) {
        this.extractedJson = extractedJson;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
