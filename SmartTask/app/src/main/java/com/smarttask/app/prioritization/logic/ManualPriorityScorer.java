package com.smarttask.app.prioritization.logic;

import com.smarttask.app.taskinput.db.Task;

public class ManualPriorityScorer {

    public float score(Task task) {
        int priority = normalizePriority(task.getPriority());
        float boost = Math.max(0, priority - 1) * 3f;
        return Math.min(boost, 20f);
    }

    private int normalizePriority(int priority) {
        if (priority >= 1 && priority <= 5) {
            return priority;
        }
        if (priority >= 2) {
            return 5;
        }
        if (priority == 1) {
            return 4;
        }
        return 1;
    }
}
