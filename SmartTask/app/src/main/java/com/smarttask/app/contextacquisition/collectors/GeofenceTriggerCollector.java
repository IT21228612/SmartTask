package com.smarttask.app.contextacquisition.collectors;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.geofence.TaskGeofenceSyncManager;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.List;

public class GeofenceTriggerCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!isGeofenceSource(ctx.sourceTrigger)) {
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
            snapshot.placeLabel = lookupLabel(ctx, fence.getRequestId());
        } else {
            Log.d(TAG, "cannot get geofenceId | reason : no triggering geofences");
            Log.d(TAG, "cannot get placeLabel | reason : no triggering geofences");
        }
    }

    private boolean isGeofenceSource(String sourceTrigger) {
        return "GEOFENCE_ENTER".equalsIgnoreCase(sourceTrigger)
                || "GEOFENCE_DWELL".equalsIgnoreCase(sourceTrigger)
                || "GEOFENCE_EXIT".equalsIgnoreCase(sourceTrigger);
    }

    @Nullable
    private String lookupLabel(CollectorContext ctx, String geofenceId) {
        Long taskId = TaskGeofenceSyncManager.parseTaskId(geofenceId);
        if (taskId == null) {
            return null;
        }
        TaskDao taskDao = TaskDatabase.getInstance(ctx.appContext).taskDao();
        Task task = taskDao.getTaskById(taskId);
        if (task == null) {
            return null;
        }
        String locationLabel = task.getLocationLabel();
        if (locationLabel != null && !locationLabel.trim().isEmpty()) {
            return locationLabel;
        }
        return task.getTitle();
    }
}
