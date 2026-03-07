package com.smarttask.app.prioritization.logic;

import com.smarttask.app.taskinput.db.Task;

public class UrgencyScorer {

    public float score(Task task, long nowMs) {
        Long dueAt = task.getDueAt();
        if (dueAt == null) {
            return 15f;
        }

        long deltaMs = dueAt - nowMs;
        long deltaMinutes = deltaMs / 60_000L;

        float score;
        if (deltaMinutes < 0) {
            score = 100f;
        } else if (deltaMinutes <= 60) {
            score = 95f;
        } else if (deltaMinutes <= 180) {
            score = 85f;
        } else if (deltaMinutes <= 24 * 60) {
            score = 75f;
        } else if (deltaMinutes <= 2 * 24 * 60) {
            score = 60f;
        } else if (deltaMinutes <= 7 * 24 * 60) {
            score = 40f;
        } else {
            score = 25f;
        }

        Integer estimatedDurationMin = task.getEstimatedDurationMin();
        if (estimatedDurationMin != null && estimatedDurationMin > 0 && deltaMinutes > 0 && estimatedDurationMin > deltaMinutes) {
            score += 10f;
        }

        return Math.min(score, 100f);
    }

    public boolean isOverdue(Task task, long nowMs) {
        return task.getDueAt() != null && task.getDueAt() < nowMs;
    }
}
