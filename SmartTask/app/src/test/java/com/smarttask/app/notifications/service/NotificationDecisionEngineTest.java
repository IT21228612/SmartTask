package com.smarttask.app.notifications.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.smarttask.app.contextmatching.model.TriggerCandidate;
import com.smarttask.app.prioritization.logic.PrioritizedTask;
import com.smarttask.app.prioritization.logic.PriorityScoreBreakdown;
import com.smarttask.app.taskinput.db.Task;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationDecisionEngineTest {

    private final NotificationDecisionEngine engine = new NotificationDecisionEngine();

    @Test
    public void selectsOnlyEligibleNotificationCandidates() {
        PrioritizedTask eligible = prioritizedTask(1L, 72f, 20f);
        PrioritizedTask blockedByInterruption = prioritizedTask(2L, 80f, 70f);

        TriggerCandidate candidate1 = new TriggerCandidate(1L, 99L, 88f,
                Collections.singletonList("DUE_WITHIN_1H"),
                Collections.emptyList(),
                "NOTIFICATION",
                true);
        TriggerCandidate candidate2 = new TriggerCandidate(2L, 99L, 85f,
                Collections.singletonList("LOCATION_INSIDE"),
                Collections.emptyList(),
                "NOTIFICATION",
                true);

        List<NotificationDecisionEngine.Decision> result = engine.selectNotificationDecisions(
                Arrays.asList(eligible, blockedByInterruption),
                Arrays.asList(candidate1, candidate2)
        );

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).prioritizedTask.task.getId());
    }

    @Test
    public void ignoresBlockedOrNonNotificationCandidates() {
        PrioritizedTask high = prioritizedTask(7L, 80f, 15f);

        TriggerCandidate blocked = new TriggerCandidate(7L, 11L, 90f,
                Collections.singletonList("OVERDUE"),
                Collections.singletonList("DND"),
                "NOTIFICATION",
                true);

        TriggerCandidate inAppOnly = new TriggerCandidate(7L, 11L, 90f,
                Collections.singletonList("OVERDUE"),
                Collections.emptyList(),
                "IN_APP",
                false);

        assertTrue(engine.selectNotificationDecisions(
                Collections.singletonList(high),
                Collections.singletonList(blocked)
        ).isEmpty());

        assertTrue(engine.selectNotificationDecisions(
                Collections.singletonList(high),
                Collections.singletonList(inAppOnly)
        ).isEmpty());
    }

    private static PrioritizedTask prioritizedTask(long taskId, float finalScore, float interruptionPenalty) {
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Task " + taskId);
        return new PrioritizedTask(task, new PriorityScoreBreakdown(60f, 60f, interruptionPenalty, 0f, finalScore), false);
    }
}
