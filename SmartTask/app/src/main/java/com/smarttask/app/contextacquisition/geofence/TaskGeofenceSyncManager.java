package com.smarttask.app.contextacquisition.geofence;

import android.content.Context;

import com.google.android.gms.location.Geofence;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskGeofenceSyncManager {

    private static final String TASK_GEOFENCE_PREFIX = "task_";

    private final TaskDao taskDao;
    private final GeofenceRegistrar geofenceRegistrar;

    public TaskGeofenceSyncManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.taskDao = TaskDatabase.getInstance(appContext).taskDao();
        this.geofenceRegistrar = new GeofenceRegistrar(appContext);
    }

    public void syncTaskGeofences() {
        List<Task> tasks = taskDao.getAllTasks();
        List<Geofence> geofences = new ArrayList<>();
        for (Task task : tasks) {
            Geofence geofence = toTaskGeofence(task);
            if (geofence != null) {
                geofences.add(geofence);
            }
        }
        geofenceRegistrar.replaceGeofences(geofences);
    }

    public static String buildTaskGeofenceId(long taskId) {
        return TASK_GEOFENCE_PREFIX + taskId;
    }

    public static Long parseTaskId(String geofenceId) {
        if (geofenceId == null || !geofenceId.startsWith(TASK_GEOFENCE_PREFIX)) {
            return null;
        }
        String suffix = geofenceId.substring(TASK_GEOFENCE_PREFIX.length());
        try {
            return Long.parseLong(suffix);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Geofence toTaskGeofence(Task task) {
        Double lat = task.getLocationLat();
        Double lng = task.getLocationLng();
        Float radiusM = task.getLocationRadius();
        if (lat == null || lng == null || radiusM == null || radiusM <= 0f) {
            return null;
        }
        return GeofenceRegistrar.buildGeofence(buildTaskGeofenceId(task.getId()), lat, lng, radiusM);
    }
}
