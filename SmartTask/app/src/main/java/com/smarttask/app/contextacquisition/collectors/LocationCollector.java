package com.smarttask.app.contextacquisition.collectors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Tasks;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.DataQualityFlags;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.concurrent.TimeUnit;

public class LocationCollector implements ContextCollector {

    private static final String TAG = "contextCollector";

    private static final long STALE_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(2);

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
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(ctx.appContext);
        Location location = getLastKnownLocation(client);
        if (location == null) {
            location = getCurrentLocation(client);
        }
        if (location != null) {
            snapshot.lat = location.getLatitude();
            snapshot.lng = location.getLongitude();
            snapshot.accuracyM = location.hasAccuracy() ? location.getAccuracy() : null;
            snapshot.speedMps = location.hasSpeed() ? location.getSpeed() : null;
            snapshot.bearingDeg = location.hasBearing() ? location.getBearing() : null;
            long ageMs = Math.abs(System.currentTimeMillis() - location.getTime());
            boolean stale = ageMs > STALE_THRESHOLD_MS;
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
            return Tasks.await(client.getLastLocation(), 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "cannot get lat | reason : getLastLocation failed " + e.getMessage());
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation(FusedLocationProviderClient client) {
        try {
            LocationRequest request = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(0);
            return Tasks.await(client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null), 3, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "cannot get lat | reason : getCurrentLocation failed " + e.getMessage());
            return null;
        }
    }
}
