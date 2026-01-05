package com.smarttask.app.contextacquisition.collectors;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import java.util.List;

public class GeofenceTriggerCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!"GEOFENCE".equalsIgnoreCase(ctx.sourceTrigger)) {
            return;
        }
        GeofencingEvent event = GeofencingEvent.fromIntent(ctx.triggerIntent);
        if (event == null || event.getTriggeringGeofences() == null) {
            Log.d(TAG, "cannot get geofenceId | reason : geofencing event unavailable");
            Log.d(TAG, "cannot get placeLabel | reason : geofencing event unavailable");
            return;
        }
        List<Geofence> geofences = event.getTriggeringGeofences();
        if (!geofences.isEmpty()) {
            Geofence fence = geofences.get(0);
            snapshot.isGeofenceHit = true;
            snapshot.geofenceId = fence.getRequestId();
            snapshot.placeLabel = lookupLabel(fence.getRequestId());
        } else {
            Log.d(TAG, "cannot get geofenceId | reason : no triggering geofences");
            Log.d(TAG, "cannot get placeLabel | reason : no triggering geofences");
        }
    }

    @Nullable
    private String lookupLabel(String geofenceId) {
        // Placeholder for real mapping lookup (e.g., from tasks table)
        if (geofenceId == null) return null;
        if (geofenceId.startsWith("task_")) {
            return "TASK_LOCATION";
        }
        return null;
    }
}
