package com.smarttask.app.contextacquisition.collectors;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Tasks;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.DataQualityFlags;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.concurrent.TimeUnit;

public class LocationCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    private static final String PERIODIC_TRIGGER = "PERIODIC";

    private static final long STALE_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(3);
    private static final long LAST_LOCATION_TIMEOUT_SECONDS = 4L;
    private static final long CURRENT_LOCATION_TIMEOUT_SECONDS = 6L;
    private static final float MIN_BEARING_SPEED_MPS = 0.5f;

    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!PermissionUtils.hasLocationPermission(ctx.appContext)) {
            snapshot.dataQualityFlags |= DataQualityFlags.NO_LOCATION;
            Log.d(TAG, "cannot get lat | reason : location permission denied");
            Log.d(TAG, "cannot get lng | reason : location permission denied");
            Log.d(TAG, "cannot get accuracyM | reason : location permission denied");
            Log.d(TAG, "cannot get speedMps | reason : location permission denied");
            Log.d(TAG, "cannot get bearingDeg | reason : location permission denied");
            return;
        }
        if (PERIODIC_TRIGGER.equals(ctx.sourceTrigger) && !PermissionUtils.hasBackgroundLocation(ctx.appContext)) {
            snapshot.dataQualityFlags |= DataQualityFlags.NO_LOCATION;
            snapshot.locationSource = "UNKNOWN";
            Log.d(TAG, "cannot get lat | reason : background location permission denied");
            Log.d(TAG, "cannot get lng | reason : background location permission denied");
            Log.d(TAG, "cannot get accuracyM | reason : background location permission denied");
            Log.d(TAG, "cannot get speedMps | reason : background location permission denied");
            Log.d(TAG, "cannot get bearingDeg | reason : background location permission denied");
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(ctx.appContext);
        Location location = getLastKnownLocation(client);
        boolean usedStaleCachedLocation = false;
        if (location == null) {
            location = getCurrentLocation(client, Priority.PRIORITY_BALANCED_POWER_ACCURACY);
            if (location == null) {
                location = getCurrentLocation(client, Priority.PRIORITY_HIGH_ACCURACY);
            }
        } else {
            long ageMs = Math.abs(System.currentTimeMillis() - location.getTime());
            boolean stale = ageMs > STALE_THRESHOLD_MS;
            if (stale) {
                Location currentLocation = getCurrentLocation(client, Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                if (currentLocation == null) {
                    currentLocation = getCurrentLocation(client, Priority.PRIORITY_HIGH_ACCURACY);
                }
                if (currentLocation != null) {
                    location = currentLocation;
                } else {
                    usedStaleCachedLocation = true;
                }
            }
        }
        if (location != null) {
            snapshot.lat = location.getLatitude();
            snapshot.lng = location.getLongitude();
            snapshot.accuracyM = location.hasAccuracy() ? location.getAccuracy() : null;
            snapshot.speedMps = location.hasSpeed() ? location.getSpeed() : null;
            boolean bearingReliable = location.hasSpeed() && location.getSpeed() > MIN_BEARING_SPEED_MPS;
            snapshot.bearingDeg = bearingReliable && location.hasBearing() ? location.getBearing() : null;
            long ageMs = Math.abs(System.currentTimeMillis() - location.getTime());
            boolean stale = ageMs > STALE_THRESHOLD_MS || usedStaleCachedLocation;
            if (snapshot.accuracyM != null && snapshot.accuracyM > 100f) {
                snapshot.dataQualityFlags |= DataQualityFlags.LOW_ACCURACY;
            }
            if (stale) {
                snapshot.dataQualityFlags |= DataQualityFlags.STALE_LOCATION;
                snapshot.locationSource = "CACHED";
            } else {
                snapshot.locationSource = "FUSED";
            }
        } else {
            snapshot.dataQualityFlags |= DataQualityFlags.NO_LOCATION;
            snapshot.locationSource = "UNKNOWN";
            Log.d(TAG, "cannot get lat | reason : fused location unavailable");
            Log.d(TAG, "cannot get lng | reason : fused location unavailable");
            Log.d(TAG, "cannot get accuracyM | reason : fused location unavailable");
            Log.d(TAG, "cannot get speedMps | reason : fused location unavailable");
            Log.d(TAG, "cannot get bearingDeg | reason : fused location unavailable");
        }
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation(FusedLocationProviderClient client) {
        try {
            return Tasks.await(client.getLastLocation(), LAST_LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "cannot get lat | reason : getLastLocation failed " + e.getMessage());
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation(FusedLocationProviderClient client, int priority) {
        try {
            return Tasks.await(client.getCurrentLocation(priority, null), CURRENT_LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "cannot get lat | reason : getCurrentLocation failed " + e.getMessage());
            return null;
        }
    }
}
