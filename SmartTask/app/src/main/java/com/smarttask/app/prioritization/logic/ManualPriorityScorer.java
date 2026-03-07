package com.smarttask.app.prioritization.logic;

import com.smarttask.app.taskinput.db.Task;

public class ManualPriorityScorer {

    public float score(Task task) {
        float boost;
        if (task.getPriority() >= 2) {
            boost = 12f;
        } else if (task.getPriority() == 1) {
            boost = 6f;
        } else {
            boost = 0f;
        }

        return Math.min(boost, 20f);
    }
}
