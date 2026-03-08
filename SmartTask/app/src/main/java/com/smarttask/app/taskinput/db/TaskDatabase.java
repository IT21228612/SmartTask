package com.smarttask.app.taskinput.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.contextmatching.db.TaskContextMatchDao;
import com.smarttask.app.voicecommandlog.db.VoiceCommandLog;
import com.smarttask.app.voicecommandlog.db.VoiceCommandLogDao;

@Database(entities = {Task.class, TaskContextMatch.class, VoiceCommandLog.class}, version = 6, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {

    private static volatile TaskDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public abstract TaskContextMatchDao taskContextMatchDao();

    public abstract VoiceCommandLogDao voiceCommandLogDao();

    public static TaskDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TaskDatabase.class, "smarttask.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
