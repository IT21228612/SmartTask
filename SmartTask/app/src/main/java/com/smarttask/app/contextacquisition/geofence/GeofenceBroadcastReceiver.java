package com.smarttask.app.contextacquisition.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.smarttask.app.contextacquisition.api.ContextEngine;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        String sourceTrigger = resolveSourceTrigger(event);
        ContextEngine.getInstance(context).captureSnapshot(sourceTrigger, intent);
    }

    private String resolveSourceTrigger(GeofencingEvent event) {
        if (event == null || event.hasError()) {
            return "GEOFENCE_ENTER";
        }
        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            return "GEOFENCE_DWELL";
        }
        if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            return "GEOFENCE_EXIT";
        }
        return "GEOFENCE_ENTER";
    }
}
