package com.smarttask.app.contextacquisition.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrivacySettings {
    private static final String PREFS = "context_privacy_settings";
    private static final String KEY_STORE_CALENDAR_TITLES = "storeCalendarTitles";
    private static final String KEY_ENABLE_NOISE = "enableNoiseSampling";
    private static final String KEY_HASH_WIFI = "hashWifiSsid";

    private final SharedPreferences prefs;

    public PrivacySettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean storeCalendarTitles() {
        return prefs.getBoolean(KEY_STORE_CALENDAR_TITLES, false);
    }

    public boolean enableNoiseSampling() {
        return prefs.getBoolean(KEY_ENABLE_NOISE, false);
    }

    public boolean hashWifiSsid() {
        return prefs.getBoolean(KEY_HASH_WIFI, true);
    }
}
