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
        float score = 0.2f;
        if (snapshot.isInMeeting) score += 0.5f;
        if ("IN_VEHICLE".equals(snapshot.activityType)) score += 0.4f;
        if (snapshot.doNotDisturbOn) score += 0.2f;
        if (!snapshot.screenOn) score += 0.1f;
        snapshot.interruptionCostScore = clamp(score);
    }

    private void computeReceptivity(ContextSnapshot snapshot) {
        float score = 0.5f;
        if (snapshot.isInMeeting) score -= 0.3f;
        if ("IN_VEHICLE".equals(snapshot.activityType)) score -= 0.2f;
        if (snapshot.appInForeground) score += 0.2f;
        if (snapshot.screenOn && snapshot.deviceUnlocked) score += 0.1f;
        snapshot.receptivityScore = clamp(score);
    }

    private String deriveLabel(ContextSnapshot snapshot) {
        if (snapshot.isInMeeting) return "MEETING";
        if ("IN_VEHICLE".equals(snapshot.activityType)) return "COMMUTING";
        if (snapshot.placeLabel != null) return "AT_" + snapshot.placeLabel.toUpperCase(Locale.US);
        if (snapshot.minuteOfDay >= 1080 && snapshot.minuteOfDay <= 1380 && snapshot.screenOn) {
            return "EVENING_ACTIVE";
        }
        return "DEFAULT";
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
