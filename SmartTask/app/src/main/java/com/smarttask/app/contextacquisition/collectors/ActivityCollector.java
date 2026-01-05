package com.smarttask.app.contextacquisition.collectors;

import android.content.Context;

import android.util.Log;

import com.smarttask.app.contextacquisition.activity.ActivityStore;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.Locale;

public class ActivityCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!PermissionUtils.hasActivityRecognition(ctx.appContext)) {
            snapshot.permissionState = appendState(snapshot.permissionState, "ACTIVITY_DENIED");
            Log.d(TAG, "cannot get activityType | reason : activity recognition permission denied");
            Log.d(TAG, "cannot get activityConfidence | reason : activity recognition permission denied");
            Log.d(TAG, "cannot get isMoving | reason : activity recognition permission denied");
            return;
        }
        ActivityStore.ActivityState state = ActivityStore.getInstance(ctx.appContext).getLastActivity();
        if (state != null) {
            snapshot.activityType = state.type;
            snapshot.activityConfidence = state.confidence;
            snapshot.isMoving = isMoving(state.type, snapshot.speedMps);
        } else {
            snapshot.activityType = "UNKNOWN";
            Log.d(TAG, "cannot get activityType | reason : no last activity state");
            Log.d(TAG, "cannot get activityConfidence | reason : no last activity state");
            Log.d(TAG, "cannot get isMoving | reason : no last activity state");
        }
    }

    private boolean isMoving(String type, Float speed) {
        if (type == null) return speed != null && speed > 1.0f;
        String t = type.toUpperCase(Locale.US);
        if (t.equals("WALKING") || t.equals("RUNNING") || t.equals("ON_BICYCLE") || t.equals("IN_VEHICLE")) {
            return true;
        }
        return speed != null && speed > 1.0f;
    }

    private String appendState(String current, String state) {
        if (current == null || current.isEmpty() || "OK".equals(current)) {
            return state;
        }
        if (current.contains(state)) {
            return current;
        }
        return current + "," + state;
    }
}
