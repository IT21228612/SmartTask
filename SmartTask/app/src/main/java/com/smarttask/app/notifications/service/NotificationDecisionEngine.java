package com.smarttask.app.notifications.service;

import androidx.annotation.NonNull;

import com.smarttask.app.contextmatching.model.TriggerCandidate;
import com.smarttask.app.prioritization.logic.PrioritizedTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDecisionEngine {

    static final float MIN_FINAL_SCORE = 45f;
    static final float MAX_INTERRUPTION_PENALTY = 55f;
    static final int MAX_NOTIFICATIONS_PER_RUN = 3;

    @NonNull
    public List<Decision> selectNotificationDecisions(@NonNull List<PrioritizedTask> prioritized,
                                                      @NonNull List<TriggerCandidate> triggerCandidates) {
        Map<Long, TriggerCandidate> candidateByTaskId = new HashMap<>();
        for (TriggerCandidate candidate : triggerCandidates) {
            candidateByTaskId.put(candidate.taskId, candidate);
        }

        List<Decision> decisions = new ArrayList<>();
        for (PrioritizedTask prioritizedTask : prioritized) {
            TriggerCandidate candidate = candidateByTaskId.get(prioritizedTask.task.getId());
            if (candidate == null) {
                continue;
            }
            if (!candidate.shouldTriggerNow || !"NOTIFICATION".equals(candidate.triggerType)) {
                continue;
            }
            if (!candidate.blockedBy.isEmpty()) {
                continue;
            }
            if (prioritizedTask.breakdown.finalScore < MIN_FINAL_SCORE) {
                continue;
            }
            if (prioritizedTask.breakdown.interruptionPenalty > MAX_INTERRUPTION_PENALTY) {
                continue;
            }

            decisions.add(new Decision(prioritizedTask, candidate));
            if (decisions.size() >= MAX_NOTIFICATIONS_PER_RUN) {
                break;
            }
        }
        return decisions;
    }

    public static class Decision {
        @NonNull
        public final PrioritizedTask prioritizedTask;
        @NonNull
        public final TriggerCandidate triggerCandidate;

        public Decision(@NonNull PrioritizedTask prioritizedTask, @NonNull TriggerCandidate triggerCandidate) {
            this.prioritizedTask = prioritizedTask;
            this.triggerCandidate = triggerCandidate;
        }
    }
}
