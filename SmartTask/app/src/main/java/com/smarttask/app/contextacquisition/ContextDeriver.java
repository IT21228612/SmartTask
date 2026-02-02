package com.smarttask.app.contextacquisition;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import java.util.Locale;

public class ContextDeriver {

    public void derive(ContextSnapshot snapshot) {
        computeInterruptionCost(snapshot);
        computeReceptivity(snapshot);
        snapshot.contextLabel = deriveLabel(snapshot);
    }

    private void computeInterruptionCost(ContextSnapshot snapshot) {
        float score = 35f;

        if (snapshot.minuteOfDay >= 1320 || snapshot.minuteOfDay <= 360) {
            score += 15f;
        } else if (snapshot.minuteOfDay >= 1080) {
            score += 5f;
        }
        if (snapshot.dayOfWeek == 6 || snapshot.dayOfWeek == 7) {
            score -= 5f;
        }

        if (snapshot.isInMeeting) {
            score += 30f;
        }
        if (snapshot.eventBusyStatus != null) {
            String busyStatus = snapshot.eventBusyStatus.toUpperCase(Locale.US);
            if (!"FREE".equals(busyStatus)) {
                score += 15f;
            }
        }
        if (snapshot.minutesLeftInEvent != null && snapshot.minutesLeftInEvent > 0) {
            score += snapshot.minutesLeftInEvent <= 15 ? 10f : 5f;
        }
        if (snapshot.minutesToNextEvent != null && snapshot.minutesToNextEvent >= 0) {
            if (snapshot.minutesToNextEvent <= 10) {
                score += 8f;
            } else if (snapshot.minutesToNextEvent <= 30) {
                score += 4f;
            }
        }

        if (snapshot.activityType != null) {
            String activity = snapshot.activityType.toUpperCase(Locale.US);
            switch (activity) {
                case "IN_VEHICLE":
                    score += 20f;
                    break;
                case "ON_BICYCLE":
                case "RUNNING":
                    score += 15f;
                    break;
                case "WALKING":
                    score += 10f;
                    break;
                case "STILL":
                    score -= 5f;
                    break;
                default:
                    score += 2f;
                    break;
            }
        }
        if (snapshot.activityConfidence >= 80) {
            score += 5f;
        } else if (snapshot.activityConfidence > 0 && snapshot.activityConfidence < 30) {
            score -= 3f;
        }
        if (snapshot.isMoving) {
            score += 8f;
        }
        if (snapshot.speedMps != null) {
            if (snapshot.speedMps > 8f) {
                score += 12f;
            } else if (snapshot.speedMps > 4f) {
                score += 6f;
            } else if (snapshot.speedMps > 1f) {
                score += 2f;
            }
        }

        if (snapshot.isGeofenceHit && snapshot.placeLabel != null) {
            String placeLabel = snapshot.placeLabel.toUpperCase(Locale.US);
            if (placeLabel.contains("HOME")) {
                score -= 10f;
            } else if (placeLabel.contains("OFFICE") || placeLabel.contains("WORK")) {
                score += 5f;
            } else {
                score += 3f;
            }
        } else if (snapshot.geofenceId != null) {
            score += 2f;
        }

        if (snapshot.doNotDisturbOn) {
            score += 12f;
        }
        if (snapshot.ringerMode != null) {
            String ringerMode = snapshot.ringerMode.toUpperCase(Locale.US);
            if (ringerMode.contains("SILENT")) {
                score += 6f;
            } else if (ringerMode.contains("VIBRATE")) {
                score += 4f;
            }
        }
        if (!snapshot.screenOn) {
            score += 10f;
        } else if (snapshot.deviceUnlocked) {
            score -= 5f;
        }
        if (snapshot.appInForeground) {
            score -= 6f;
        }

        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 20) {
            score += 5f;
        }
        if (snapshot.isCharging) {
            score -= 4f;
        }
        if (snapshot.powerSaveMode) {
            score += 4f;
        }

        if (snapshot.connectivityType != null) {
            String connectivity = snapshot.connectivityType.toUpperCase(Locale.US);
            if ("NONE".equals(connectivity)) {
                score += 6f;
            } else if (connectivity.contains("WIFI")) {
                score -= 3f;
            } else if (connectivity.contains("MOBILE")) {
                score += 2f;
            }
        }
        if (!snapshot.isInternetAvailable) {
            score += 5f;
        }
        if (Boolean.TRUE.equals(snapshot.isRoaming)) {
            score += 3f;
        }

        if (Boolean.TRUE.equals(snapshot.headphonesConnected)) {
            score += 4f;
        }
        if (Boolean.TRUE.equals(snapshot.bluetoothConnected)) {
            score += 3f;
        }
        if (snapshot.noiseLevelDb != null) {
            if (snapshot.noiseLevelDb > 70f) {
                score += 10f;
            } else if (snapshot.noiseLevelDb > 55f) {
                score += 5f;
            }
        }

        if (snapshot.locationSource != null && "UNKNOWN".equals(snapshot.locationSource)) {
            score += 4f;
        }
        if (snapshot.accuracyM != null && snapshot.accuracyM > 100f) {
            score += 2f;
        }
        if (snapshot.permissionState != null && !"OK".equals(snapshot.permissionState)) {
            score += 4f;
        }
        if (snapshot.dataQualityFlags != 0) {
            score += 2f;
        }
        if (snapshot.anonymized) {
            score += 1f;
        }
        if (snapshot.sourceTrigger != null && snapshot.sourceTrigger.toUpperCase(Locale.US).contains("USER")) {
            score -= 2f;
        }

        snapshot.interruptionCostScore = clampScore(score);
    }

    private void computeReceptivity(ContextSnapshot snapshot) {
        float score = 50f;

        if (snapshot.minuteOfDay >= 540 && snapshot.minuteOfDay <= 1020) {
            score += 6f;
        } else if (snapshot.minuteOfDay >= 1320 || snapshot.minuteOfDay <= 360) {
            score -= 12f;
        }
        if (snapshot.dayOfWeek == 6 || snapshot.dayOfWeek == 7) {
            score += 3f;
        }

        if (snapshot.isInMeeting) {
            score -= 30f;
        }
        if (snapshot.eventBusyStatus != null) {
            String busyStatus = snapshot.eventBusyStatus.toUpperCase(Locale.US);
            if (!"FREE".equals(busyStatus)) {
                score -= 15f;
            }
        }
        if (snapshot.minutesLeftInEvent != null && snapshot.minutesLeftInEvent > 0) {
            score -= snapshot.minutesLeftInEvent <= 15 ? 10f : 5f;
        }
        if (snapshot.minutesToNextEvent != null && snapshot.minutesToNextEvent >= 0) {
            if (snapshot.minutesToNextEvent <= 10) {
                score -= 6f;
            } else if (snapshot.minutesToNextEvent <= 30) {
                score -= 3f;
            }
        }

        if (snapshot.activityType != null) {
            String activity = snapshot.activityType.toUpperCase(Locale.US);
            switch (activity) {
                case "IN_VEHICLE":
                    score -= 20f;
                    break;
                case "ON_BICYCLE":
                case "RUNNING":
                    score -= 15f;
                    break;
                case "WALKING":
                    score -= 8f;
                    break;
                case "STILL":
                    score += 5f;
                    break;
                default:
                    score -= 2f;
                    break;
            }
        }
        if (snapshot.activityConfidence >= 80) {
            score += 3f;
        } else if (snapshot.activityConfidence > 0 && snapshot.activityConfidence < 30) {
            score -= 3f;
        }
        if (snapshot.isMoving) {
            score -= 8f;
        }
        if (snapshot.speedMps != null) {
            if (snapshot.speedMps > 8f) {
                score -= 10f;
            } else if (snapshot.speedMps > 4f) {
                score -= 6f;
            } else if (snapshot.speedMps > 1f) {
                score -= 2f;
            }
        }

        if (snapshot.isGeofenceHit && snapshot.placeLabel != null) {
            String placeLabel = snapshot.placeLabel.toUpperCase(Locale.US);
            if (placeLabel.contains("HOME")) {
                score += 10f;
            } else if (placeLabel.contains("OFFICE") || placeLabel.contains("WORK")) {
                score += 5f;
            } else {
                score += 2f;
            }
        } else if (snapshot.geofenceId != null) {
            score += 2f;
        }

        if (snapshot.doNotDisturbOn) {
            score -= 15f;
        }
        if (snapshot.ringerMode != null) {
            String ringerMode = snapshot.ringerMode.toUpperCase(Locale.US);
            if (ringerMode.contains("SILENT")) {
                score -= 6f;
            } else if (ringerMode.contains("VIBRATE")) {
                score -= 4f;
            }
        }
        if (snapshot.screenOn) {
            score += 8f;
        } else {
            score -= 10f;
        }
        if (snapshot.deviceUnlocked) {
            score += 6f;
        }
        if (snapshot.appInForeground) {
            score += 8f;
        }

        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 20) {
            score -= 5f;
        }
        if (snapshot.isCharging) {
            score += 4f;
        }
        if (snapshot.powerSaveMode) {
            score -= 4f;
        }

        if (snapshot.connectivityType != null) {
            String connectivity = snapshot.connectivityType.toUpperCase(Locale.US);
            if ("NONE".equals(connectivity)) {
                score -= 6f;
            } else if (connectivity.contains("WIFI")) {
                score += 5f;
            } else if (connectivity.contains("MOBILE")) {
                score += 2f;
            }
        }
        if (snapshot.isInternetAvailable) {
            score += 4f;
        } else {
            score -= 4f;
        }
        if (Boolean.TRUE.equals(snapshot.isRoaming)) {
            score -= 3f;
        }

        if (Boolean.TRUE.equals(snapshot.headphonesConnected)) {
            score -= 2f;
        }
        if (Boolean.TRUE.equals(snapshot.bluetoothConnected)) {
            score -= 1f;
        }
        if (snapshot.noiseLevelDb != null) {
            if (snapshot.noiseLevelDb > 70f) {
                score -= 8f;
            } else if (snapshot.noiseLevelDb > 55f) {
                score -= 4f;
            }
        }

        if (snapshot.locationSource != null && "UNKNOWN".equals(snapshot.locationSource)) {
            score -= 2f;
        }
        if (snapshot.accuracyM != null && snapshot.accuracyM > 100f) {
            score -= 2f;
        }
        if (snapshot.permissionState != null && !"OK".equals(snapshot.permissionState)) {
            score -= 3f;
        }
        if (snapshot.dataQualityFlags != 0) {
            score -= 2f;
        }
        if (snapshot.anonymized) {
            score -= 1f;
        }
        if (snapshot.sourceTrigger != null && snapshot.sourceTrigger.toUpperCase(Locale.US).contains("USER")) {
            score += 4f;
        }

        snapshot.receptivityScore = clampScore(score);
    }

    private String deriveLabel(ContextSnapshot snapshot) {
        if (snapshot.isInMeeting) {
            return "MEETING";
        }
        if ("IN_VEHICLE".equals(snapshot.activityType)) {
            return "COMMUTING";
        }
        if (snapshot.placeLabel != null) {
            return "AT_" + snapshot.placeLabel.toUpperCase(Locale.US);
        }
        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 20) {
            return "LOW_BATTERY";
        }
        if (snapshot.connectivityType != null
                && ("NONE".equals(snapshot.connectivityType) || !snapshot.isInternetAvailable)) {
            return "NO_CONNECTIVITY";
        }
        if (snapshot.screenOn && snapshot.deviceUnlocked) {
            return "AVAILABLE_ACTIVE";
        }
        if (!snapshot.screenOn) {
            return "IDLE_INACTIVE";
        }
        if (snapshot.minuteOfDay >= 1080 && snapshot.minuteOfDay <= 1380 && snapshot.screenOn) {
            return "EVENING_ACTIVE";
        }
        return "DEFAULT";
    }

    private float clampScore(float value) {
        return Math.max(0f, Math.min(100f, value));
    }
}
