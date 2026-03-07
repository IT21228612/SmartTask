package com.smarttask.app.prioritization.logic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskPrioritizationRunner {

    private final Context appContext;
    private final TaskDatabase taskDatabase;
    private final TaskDao taskDao;
    private final TaskScorer taskScorer;
    private final DisplayOrderUpdater displayOrderUpdater;

    public TaskPrioritizationRunner(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.taskDatabase = TaskDatabase.getInstance(appContext);
        this.taskDao = taskDatabase.taskDao();
        this.taskScorer = new TaskScorer();
        this.displayOrderUpdater = new DisplayOrderUpdater();
    }

    public List<PrioritizedTask> run(long snapshotId) {
        ContextSnapshot snapshot = ContextDatabase.getInstance(appContext)
                .contextSnapshotDao()
                .getById(snapshotId);
        return run(snapshotId, snapshot);
    }

    public List<PrioritizedTask> run(long snapshotId, @Nullable ContextSnapshot snapshot) {
        long nowMs = System.currentTimeMillis();
        List<Task> activeTasks = taskDao.getActiveIncompleteTasksForPrioritization();
        List<TaskContextMatch> matches = taskDatabase.taskContextMatchDao().getBySnapshotId(snapshotId);

        Map<Long, TaskContextMatch> matchByTaskId = new HashMap<>();
        for (TaskContextMatch match : matches) {
            matchByTaskId.put(match.taskId, match);
        }

        List<PrioritizedTask> prioritized = new ArrayList<>();
        for (Task task : activeTasks) {
            prioritized.add(taskScorer.score(task, snapshot, matchByTaskId.get(task.getId()), nowMs));
        }

        prioritized.sort(buildComparator());
        taskDatabase.runInTransaction(() -> displayOrderUpdater.update(taskDao, prioritized));
        return prioritized;
    }

    private Comparator<PrioritizedTask> buildComparator() {
        return (left, right) -> {
            float scoreDiff = right.breakdown.finalScore - left.breakdown.finalScore;
            if (Math.abs(scoreDiff) > PriorityWeightsProvider.ANTI_JITTER_THRESHOLD) {
                return scoreDiff > 0 ? 1 : -1;
            }

            if (left.overdue != right.overdue) {
                return left.overdue ? -1 : 1;
            }

            Long leftDue = left.task.getDueAt();
            Long rightDue = right.task.getDueAt();
            if (leftDue != null || rightDue != null) {
                if (leftDue == null) return 1;
                if (rightDue == null) return -1;
                int dueCompare = Long.compare(leftDue, rightDue);
                if (dueCompare != 0) {
                    return dueCompare;
                }
            }

            int manualCompare = Integer.compare(right.task.getPriority(), left.task.getPriority());
            if (manualCompare != 0) {
                return manualCompare;
            }

            int displayOrderCompare = Long.compare(left.task.getDisplayOrder(), right.task.getDisplayOrder());
            if (displayOrderCompare != 0) {
                return displayOrderCompare;
            }

            return Long.compare(right.task.getUpdatedAt(), left.task.getUpdatedAt());
        };
    }
}
