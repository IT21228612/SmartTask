package com.smarttask.app.contextmatching.logic;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.DataQualityFlags;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.contextmatching.model.MatchResult;
import com.smarttask.app.taskinput.db.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContextMatcher {

    private static final float IN_APP_THRESHOLD = 35f;
    private static final float NOTIFICATION_THRESHOLD = 65f;
    private static final long COOLDOWN_MS = 60 * 60 * 1000L;
    private static final float DEFAULT_RADIUS_M = 150f;

    public MatchResult match(ContextSnapshot snapshot, Task task, long nowMs, @Nullable TaskContextMatch previousMatch) {
        float score = 0f;
        List<String> reasons = new ArrayList<>();
        List<String> blockedBy = new ArrayList<>();

        score += scoreLocation(snapshot, task, reasons);
        score += scoreTime(task, nowMs, reasons);
        score += scoreCalendar(snapshot, task, nowMs, reasons, blockedBy);
        score += scoreActivity(snapshot, task, nowMs, blockedBy);
        score += scoreDeviceState(snapshot, task, nowMs, blockedBy);

        float clamped = clamp(score, 0f, 100f);

        boolean blocked = !blockedBy.isEmpty();
        boolean inCooldown = previousMatch != null && previousMatch.cooldownUntil != null
                && previousMatch.cooldownUntil > nowMs;
        if (inCooldown) {
            blockedBy.add("COOLDOWN");
            blocked = true;
        }

        boolean shouldTrigger = clamped >= NOTIFICATION_THRESHOLD && !blocked;
        Long cooldownUntil = shouldTrigger ? nowMs + COOLDOWN_MS : null;
        String triggerType;
        if (shouldTrigger) {
            triggerType = "NOTIFICATION";
        } else if (clamped >= IN_APP_THRESHOLD) {
            triggerType = "IN_APP";
        } else {
            triggerType = "NONE";
        }
        return new MatchResult(clamped, reasons, blockedBy, shouldTrigger, triggerType, cooldownUntil);
    }

    private float scoreLocation(ContextSnapshot snapshot, Task task, List<String> reasons) {
        if (snapshot.lat == null || snapshot.lng == null || task.getLocationLat() == null || task.getLocationLng() == null) {
            return 0f;
        }

        double distanceM = haversineMeters(snapshot.lat, snapshot.lng, task.getLocationLat(), task.getLocationLng());
        float radius = task.getLocationRadius() != null ? task.getLocationRadius() : DEFAULT_RADIUS_M;

        float locationScore = 0f;
        if (distanceM <= radius) {
            locationScore = 40f;
            reasons.add("LOCATION_INSIDE");
        } else if (distanceM <= radius * 2f) {
            locationScore = 20f;
            reasons.add("LOCATION_NEAR");
        }

        if ((snapshot.dataQualityFlags & DataQualityFlags.NO_LOCATION) != 0
                || (snapshot.dataQualityFlags & DataQualityFlags.STALE_LOCATION) != 0
                || (snapshot.accuracyM != null && snapshot.accuracyM > 100f)) {
            locationScore *= 0.5f;
            reasons.add("LOCATION_LOW_CONFIDENCE");
        }

        return locationScore;
    }

    private float scoreTime(Task task, long nowMs, List<String> reasons) {
        float total = 0f;

        if (task.getDueAt() != null) {
            long minsToDue = (task.getDueAt() - nowMs) / 60_000L;
            if (minsToDue < 0) {
                total += 35f;
                reasons.add("OVERDUE");
            } else if (minsToDue <= 60) {
                total += 30f;
                reasons.add("DUE_WITHIN_1H");
            } else if (minsToDue <= 180) {
                total += 22f;
                reasons.add("DUE_WITHIN_3H");
            } else if (minsToDue <= 1440) {
                total += 15f;
                reasons.add("DUE_WITHIN_24H");
            } else if (minsToDue <= 4320) {
                total += 8f;
                reasons.add("DUE_WITHIN_3D");
            }
        }

        if (task.getPreferredStartTime() != null && task.getPreferredEndTime() != null) {
            long start = task.getPreferredStartTime();
            long end = task.getPreferredEndTime();
            if (nowMs >= start && nowMs <= end) {
                total += 10f;
                reasons.add("PREFERRED_WINDOW");
            } else if (nowMs < start && start - nowMs <= 30 * 60 * 1000L) {
                total += 5f;
                reasons.add("WINDOW_SOON");
            }
        }

        return total;
    }

    private float scoreCalendar(ContextSnapshot snapshot, Task task, long nowMs, List<String> reasons, List<String> blockedBy) {
        boolean busy = snapshot.isInMeeting || "BUSY".equalsIgnoreCase(snapshot.eventBusyStatus);
        if (!busy) {
            return 0f;
        }

        boolean urgentTask = task.getDueAt() != null && task.getDueAt() < nowMs;
        if (!urgentTask && task.getPriority() < 8) {
            blockedBy.add("MEETING");
        }
        reasons.add("CALENDAR_BUSY");
        return -30f;
    }

    private float scoreActivity(ContextSnapshot snapshot, Task task, long nowMs, List<String> blockedBy) {
        if (snapshot.activityType == null) {
            return 0f;
        }
        String activity = snapshot.activityType.toUpperCase(Locale.US);
        if (activity.contains("IN_VEHICLE")) {
            boolean overdue = task.getDueAt() != null && task.getDueAt() < nowMs;
            if (!overdue) {
                blockedBy.add("DRIVING");
            }
            return -15f;
        }
        if (activity.contains("RUNNING")) {
            return -8f;
        }
        return 0f;
    }

    private float scoreDeviceState(ContextSnapshot snapshot, Task task, long nowMs, List<String> blockedBy) {
        float total = 0f;
        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 15 && !snapshot.isCharging) {
            boolean overdue = task.getDueAt() != null && task.getDueAt() < nowMs;
            total -= 10f;
            if (!overdue) {
                blockedBy.add("LOW_BATTERY");
            }
        }
        if (!snapshot.isInternetAvailable) {
            total -= 5f;
            blockedBy.add("OFFLINE");
        }
        if (snapshot.doNotDisturbOn) {
            blockedBy.add("DND");
        }
        return total;
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
