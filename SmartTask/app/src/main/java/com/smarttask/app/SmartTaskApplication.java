package com.smarttask.app;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
import com.smarttask.app.contextacquisition.api.ContextEngine;
import com.smarttask.app.contextacquisition.workers.ContextSnapshotWorker;

public class SmartTaskApplication extends Application {
    private static final String TAG = "SmartTaskApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initializePlacesSdk();
        ContextEngine contextEngine = ContextEngine.getInstance(this);
        contextEngine.registerBackgroundCollectors();
        contextEngine.captureSnapshot("APP_START", null);
        ContextSnapshotWorker.schedule(this);
    }

    private void initializePlacesSdk() {
        if (Places.isInitialized()) {
            return;
        }
        String apiKey = getMapsApiKey();
        if (TextUtils.isEmpty(apiKey)) {
            Log.w(TAG, "Maps API key missing; Places SDK not initialized.");
            return;
        }
        Places.initialize(getApplicationContext(), apiKey);
    }

    private String getMapsApiKey() {
        try {
            ApplicationInfo appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }
}
