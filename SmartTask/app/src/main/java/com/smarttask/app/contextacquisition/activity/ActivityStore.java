package com.smarttask.app.contextacquisition.activity;

import android.content.Context;
import android.content.SharedPreferences;

public class ActivityStore {
    private static final String PREFS = "context_activity_store";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CONFIDENCE = "confidence";

    private static ActivityStore INSTANCE;
    private final SharedPreferences prefs;

    public static class ActivityState {
        public final String type;
        public final int confidence;

        public ActivityState(String type, int confidence) {
            this.type = type;
            this.confidence = confidence;
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
        prefs.edit().putString(KEY_TYPE, type).putInt(KEY_CONFIDENCE, confidence).apply();
    }

    public ActivityState getLastActivity() {
        String type = prefs.getString(KEY_TYPE, null);
        int confidence = prefs.getInt(KEY_CONFIDENCE, 0);
        if (type == null) return null;
        return new ActivityState(type, confidence);
    }
}
