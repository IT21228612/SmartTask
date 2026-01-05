package com.smarttask.app.contextacquisition.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class GeofenceRegistrar {

    private final Context appContext;
    private final GeofencingClient client;

    public GeofenceRegistrar(Context context) {
        this.appContext = context.getApplicationContext();
        this.client = LocationServices.getGeofencingClient(appContext);
    }

    public void registerGeofences(List<Geofence> fences) {
        if (!PermissionUtils.hasLocationPermission(appContext) || !PermissionUtils.hasBackgroundLocation(appContext)) {
            return;
        }
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(fences)
                .build();
        client.addGeofences(request, getPendingIntent());
    }

    public PendingIntent getPendingIntent() {
        Intent intent = new Intent(appContext, GeofenceBroadcastReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.getBroadcast(appContext, 0, intent, flags);
    }

    public static Geofence buildGeofence(String id, double lat, double lng, float radiusMeters) {
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lng, radiusMeters)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}
