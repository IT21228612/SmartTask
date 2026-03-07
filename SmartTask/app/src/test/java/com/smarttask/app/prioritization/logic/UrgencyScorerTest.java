package com.smarttask.app.prioritization.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.smarttask.app.taskinput.db.Task;

import org.junit.Test;

public class UrgencyScorerTest {

    private final UrgencyScorer scorer = new UrgencyScorer();

    @Test
    public void overdueTaskGetsMaxUrgency() {
        Task task = new Task();
        task.setDueAt(System.currentTimeMillis() - 1_000L);

        float score = scorer.score(task, System.currentTimeMillis());

        assertEquals(100f, score, 0.01f);
        assertTrue(scorer.isOverdue(task, System.currentTimeMillis()));
    }

    @Test
    public void noDueDateGetsLowUrgency() {
        Task task = new Task();

        float score = scorer.score(task, System.currentTimeMillis());

        assertEquals(15f, score, 0.01f);
    }
}
