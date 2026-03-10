package com.smarttask.app.contextmatching.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.contextmatching.model.MatchResult;
import com.smarttask.app.taskinput.db.Task;

import org.junit.Test;

public class ContextMatcherTest {

    private final ContextMatcher matcher = new ContextMatcher();

    @Test
    public void keepsPreviousCooldownUntilWhenBlockedByCooldown() {
        long now = 1_700_000_000_000L;

        Task task = new Task();
        task.setPriority(9);
        task.setDueAt(now + 30 * 60 * 1000L);

        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.isInternetAvailable = true;

        TaskContextMatch previous = new TaskContextMatch();
        previous.cooldownUntil = now + 10 * 60 * 1000L;

        MatchResult result = matcher.match(snapshot, task, now, previous);

        assertTrue(result.blockedBy.contains("COOLDOWN"));
        assertEquals(previous.cooldownUntil, result.cooldownUntil);
    }
}
