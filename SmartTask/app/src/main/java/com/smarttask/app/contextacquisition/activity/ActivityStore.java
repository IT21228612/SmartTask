package com.smarttask.app.contextacquisition.activity;

import android.content.Context;
import android.content.SharedPreferences;

public class ActivityStore {
    private static final String PREFS = "context_activity_store";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CONFIDENCE = "confidence";

    private static final String KEY_TIMESTAMP = "timestamp"; // new key

    private static ActivityStore INSTANCE;
    private final SharedPreferences prefs;

    public static class ActivityState {
        public final String type;
        public final int confidence;
        public final long timestamp; // new field

        public ActivityState(String type, int confidence, long timestamp) {
            this.type = type;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
    }

    private ActivityStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static ActivityStore getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ActivityStore(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public void update(String type, int confidence) {
        long now = System.currentTimeMillis(); // record current time
        prefs.edit().putString(KEY_TYPE, type).putInt(KEY_CONFIDENCE, confidence) .putLong(KEY_TIMESTAMP, now).apply();
    }

    public ActivityState getLastActivity() {
        String type = prefs.getString(KEY_TYPE, null);
        int confidence = prefs.getInt(KEY_CONFIDENCE, 0);
        long timestamp = prefs.getLong(KEY_TIMESTAMP, 0); // fetch stored timestamp
        if (type == null) return null;
        return new ActivityState(type, confidence, timestamp);
    }
}
