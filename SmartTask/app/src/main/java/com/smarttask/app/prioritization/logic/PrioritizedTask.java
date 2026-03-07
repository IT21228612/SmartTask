package com.smarttask.app.prioritization.logic;

import androidx.annotation.NonNull;

import com.smarttask.app.taskinput.db.Task;

public class PrioritizedTask {
    @NonNull
    public final Task task;
    @NonNull
    public final PriorityScoreBreakdown breakdown;
    public final boolean overdue;

    public PrioritizedTask(@NonNull Task task, @NonNull PriorityScoreBreakdown breakdown, boolean overdue) {
        this.task = task;
        this.breakdown = breakdown;
        this.overdue = overdue;
    }
}
