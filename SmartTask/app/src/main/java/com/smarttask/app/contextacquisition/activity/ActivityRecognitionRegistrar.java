package com.smarttask.app.contextacquisition.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.concurrent.TimeUnit;

public class ActivityRecognitionRegistrar {

    private static final String TAG = "contextCollector";
    private static final long DETECTION_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    private static ActivityRecognitionRegistrar INSTANCE;

    private final Context appContext;
    private final ActivityRecognitionClient client;

    private boolean updatesRequested = false;

    private ActivityRecognitionRegistrar(Context context) {
        this.appContext = context.getApplicationContext();
        this.client = ActivityRecognition.getClient(appContext);
    }

    public static synchronized ActivityRecognitionRegistrar getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ActivityRecognitionRegistrar(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public void ensureUpdatesRequested() {
        if (updatesRequested) {
            return;
        }
        if (!PermissionUtils.hasActivityRecognition(appContext)) {
            Log.d(TAG, "skip activity updates | reason : activity recognition permission denied");
            return;
        }
        updatesRequested = true;
        client.requestActivityUpdates(DETECTION_INTERVAL_MS, getPendingIntent())
                .addOnFailureListener(e -> {
                    updatesRequested = false;
                    Log.d(TAG, "failed to request activity updates: " + e.getMessage());
                });
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(appContext, ActivityRecognitionReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.getBroadcast(appContext, 0, intent, flags);
    }
}
