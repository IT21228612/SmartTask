package com.smarttask.app.contextacquisition.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ContextSnapshot.class}, version = 1, exportSchema = false)
public abstract class ContextDatabase extends RoomDatabase {

    private static volatile ContextDatabase INSTANCE;

    public abstract ContextSnapshotDao contextSnapshotDao();

    public static ContextDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ContextDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ContextDatabase.class,
                            "context_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
