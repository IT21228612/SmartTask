package com.smarttask.app.prioritization.logic;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import java.util.Locale;

public class InterruptionCostScorer {

    public float score(@Nullable ContextSnapshot snapshot) {
        if (snapshot == null) {
            return 10f;
        }

        if (snapshot.activityType != null && snapshot.activityType.toUpperCase(Locale.US).contains("IN_VEHICLE")) {
            return 80f;
        }
        if (snapshot.isInMeeting || "BUSY".equalsIgnoreCase(snapshot.eventBusyStatus)) {
            return 65f;
        }
        if (snapshot.activityType != null && snapshot.activityType.toUpperCase(Locale.US).contains("RUNNING")) {
            return 40f;
        }
        if (snapshot.doNotDisturbOn) {
            return 50f;
        }
        if (snapshot.batteryPct > 0 && snapshot.batteryPct < 15 && !snapshot.isCharging) {
            return 30f;
        }
        return 10f;
    }
}
