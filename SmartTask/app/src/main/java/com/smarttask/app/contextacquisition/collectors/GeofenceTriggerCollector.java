package com.smarttask.app.contextacquisition.collectors;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import java.util.List;

public class GeofenceTriggerCollector implements ContextCollector {
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!"GEOFENCE".equalsIgnoreCase(ctx.sourceTrigger)) {
            return;
        }
        GeofencingEvent event = GeofencingEvent.fromIntent(ctx.triggerIntent);
        if (event == null || event.getTriggeringGeofences() == null) {
            return;
        }
        List<Geofence> geofences = event.getTriggeringGeofences();
        if (!geofences.isEmpty()) {
            Geofence fence = geofences.get(0);
            snapshot.isGeofenceHit = true;
            snapshot.geofenceId = fence.getRequestId();
            snapshot.placeLabel = lookupLabel(fence.getRequestId());
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
