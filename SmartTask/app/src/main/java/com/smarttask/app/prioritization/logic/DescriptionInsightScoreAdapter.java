package com.smarttask.app.prioritization.logic;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.descriptioninsights.db.TaskDescriptionInsight;

import java.util.Locale;

public class DescriptionInsightScoreAdapter {

    public float scoreBoost(@Nullable TaskDescriptionInsight insight, @Nullable ContextSnapshot snapshot) {
        if (insight == null) return 0f;

        float boost = (insight.riskIfDelayed / 100f) * 12f;

        if (insight.requiresDeepFocus && snapshot != null && snapshot.activityType != null) {
            String activity = snapshot.activityType.toUpperCase(Locale.US);
            if (activity.contains("IN_VEHICLE") || activity.contains("RUNNING") || snapshot.isInMeeting) {
                boost -= 8f;
            } else {
                boost += 2f;
            }
        }

        if (insight.dependencyBlocked) {
            boost -= 6f;
        }

        return clamp(boost, -12f, 12f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
