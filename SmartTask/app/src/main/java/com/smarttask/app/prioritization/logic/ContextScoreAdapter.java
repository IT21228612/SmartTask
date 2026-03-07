package com.smarttask.app.prioritization.logic;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.taskinput.db.Task;

import java.util.Locale;

public class ContextScoreAdapter {

    private static final float DEFAULT_RADIUS_M = 150f;

    public float score(@Nullable ContextSnapshot snapshot, @Nullable TaskContextMatch match, Task task, long nowMs) {
        if (snapshot == null) {
            return 50f;
        }

        float locationFit = scoreLocationFit(snapshot, task);
        float timeFit = scoreTimeFit(snapshot, task, nowMs);
        float activityFit = scoreActivityFit(snapshot);
        float deviceFit = scoreDeviceFit(snapshot);

        if (match != null) {
            float normalizedMatch = clamp(match.relevanceScore, 0f, 100f);
            locationFit = blend(locationFit, normalizedMatch, 0.35f);
            timeFit = blend(timeFit, normalizedMatch, 0.35f);
        }

        return clamp(
                0.40f * locationFit
                        + 0.30f * timeFit
                        + 0.20f * activityFit
                        + 0.10f * deviceFit,
                0f,
                100f
        );
    }

    private float scoreLocationFit(ContextSnapshot snapshot, Task task) {
        if (snapshot.lat == null || snapshot.lng == null || task.getLocationLat() == null || task.getLocationLng() == null) {
            return 50f;
        }

        double distanceM = haversineMeters(snapshot.lat, snapshot.lng, task.getLocationLat(), task.getLocationLng());
        float radius = task.getLocationRadius() != null ? task.getLocationRadius() : DEFAULT_RADIUS_M;

        if (distanceM <= radius) {
            return 95f;
        }
        if (distanceM <= radius * 2f) {
            return 75f;
        }
        return 35f;
    }

    private float scoreTimeFit(ContextSnapshot snapshot, Task task, long nowMs) {
        if (task.getPreferredStartTime() == null || task.getPreferredEndTime() == null) {
            return 50f;
        }

        long start = task.getPreferredStartTime();
        long end = task.getPreferredEndTime();
        if (nowMs >= start && nowMs <= end) {
            return 95f;
        }

        long toWindowMinutes = (start - nowMs) / 60_000L;
        if (toWindowMinutes >= 0 && toWindowMinutes <= 60) {
            return 70f;
        }

        return snapshot.isInMeeting ? 30f : 40f;
    }

    private float scoreActivityFit(ContextSnapshot snapshot) {
        if (snapshot.activityType == null) {
            return 50f;
        }

        String activity = snapshot.activityType.toUpperCase(Locale.US);
        if (activity.contains("IN_VEHICLE")) {
            return 20f;
        }
        if (activity.contains("RUNNING") || activity.contains("WALKING")) {
            return 40f;
        }
        if (activity.contains("STILL")) {
            return 70f;
        }
        return 50f;
    }

    private float scoreDeviceFit(ContextSnapshot snapshot) {
        float fit = 50f;
        if (snapshot.doNotDisturbOn) {
            fit -= 25f;
        }
        if (snapshot.isCharging) {
            fit += 10f;
        }
        if (!snapshot.isInternetAvailable) {
            fit -= 10f;
        }
        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 15) {
            fit -= 15f;
        }
        return clamp(fit, 0f, 100f);
    }

    private static float blend(float base, float byMatch, float factor) {
        return ((1f - factor) * base) + (factor * byMatch);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}
