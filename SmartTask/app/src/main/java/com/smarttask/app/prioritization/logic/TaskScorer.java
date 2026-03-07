package com.smarttask.app.prioritization.logic;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.taskinput.db.Task;

public class TaskScorer {

    private final UrgencyScorer urgencyScorer = new UrgencyScorer();
    private final ContextScoreAdapter contextScoreAdapter = new ContextScoreAdapter();
    private final InterruptionCostScorer interruptionCostScorer = new InterruptionCostScorer();
    private final ManualPriorityScorer manualPriorityScorer = new ManualPriorityScorer();

    public PrioritizedTask score(Task task, @Nullable ContextSnapshot snapshot, @Nullable TaskContextMatch match, long nowMs) {
        float urgency = urgencyScorer.score(task, nowMs);
        float context = contextScoreAdapter.score(snapshot, match, task, nowMs);
        float interruptionPenalty = interruptionCostScorer.score(snapshot);
        float manualBoost = manualPriorityScorer.score(task);

        float finalScore = (PriorityWeightsProvider.URGENCY_WEIGHT * urgency)
                + (PriorityWeightsProvider.CONTEXT_WEIGHT * context)
                - (PriorityWeightsProvider.INTERRUPTION_WEIGHT * interruptionPenalty)
                + manualBoost;

        PriorityScoreBreakdown breakdown = new PriorityScoreBreakdown(
                urgency,
                context,
                interruptionPenalty,
                manualBoost,
                finalScore
        );

        return new PrioritizedTask(task, breakdown, urgencyScorer.isOverdue(task, nowMs));
    }
}
