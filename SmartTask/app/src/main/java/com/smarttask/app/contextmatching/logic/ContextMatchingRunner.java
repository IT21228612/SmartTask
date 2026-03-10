package com.smarttask.app.contextmatching.logic;

import android.content.Context;

import androidx.annotation.NonNull;

import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextmatching.db.TaskContextMatch;
import com.smarttask.app.contextmatching.db.TaskContextMatchDao;
import com.smarttask.app.contextmatching.model.MatchResult;
import com.smarttask.app.contextmatching.model.TriggerCandidate;
import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContextMatchingRunner {

    private static final double MAX_RADIUS_METERS = 1000d;

    private final TaskDao taskDao;
    private final TaskContextMatchDao matchDao;
    private final ContextMatcher matcher;

    public ContextMatchingRunner(@NonNull Context context) {
        TaskDatabase taskDatabase = TaskDatabase.getInstance(context.getApplicationContext());
        this.taskDao = taskDatabase.taskDao();
        this.matchDao = taskDatabase.taskContextMatchDao();
        this.matcher = new ContextMatcher();
    }

    public List<TriggerCandidate> runForSnapshot(@NonNull Context context, long snapshotId) {
        ContextSnapshot snapshot = ContextDatabase.getInstance(context.getApplicationContext())
                .contextSnapshotDao()
                .getById(snapshotId);
        if (snapshot == null) {
            return new ArrayList<>();
        }
        return run(snapshot);
    }

    public List<TriggerCandidate> run(@NonNull ContextSnapshot snapshot) {
        long now = System.currentTimeMillis();
        List<Task> activeTasks = fetchCandidates(snapshot, now);

        List<TaskContextMatch> toPersist = new ArrayList<>();
        List<TriggerCandidate> triggerCandidates = new ArrayList<>();
        for (Task task : activeTasks) {
            TaskContextMatch previous = matchDao.getLatestForTask(task.getId());
            int previousNotificationCount = matchDao.getNotificationTriggerCountForTask(task.getId());
            MatchResult result = matcher.match(snapshot, task, now, previous, previousNotificationCount);
            TaskContextMatch dbMatch = toEntity(task.getId(), snapshot, now, result);
            toPersist.add(dbMatch);

            if ("IN_APP".equals(result.triggerType) || "NOTIFICATION".equals(result.triggerType)) {
                triggerCandidates.add(new TriggerCandidate(
                        task.getId(),
                        snapshot.id,
                        result.relevanceScore,
                        result.reasons,
                        result.blockedBy,
                        result.triggerType,
                        result.shouldTriggerNow
                ));
            }
        }

        if (!toPersist.isEmpty()) {
            matchDao.insertAll(toPersist);
        }
        matchDao.deleteOlderThan(now - 30L * 24L * 60L * 60L * 1000L);

        return triggerCandidates;
    }

    private List<Task> fetchCandidates(ContextSnapshot snapshot, long now) {
        List<Task> general = taskDao.getActiveTasksForMatching(now);
        if (snapshot.lat == null || snapshot.lng == null) {
            return general;
        }

        Bounds bounds = buildBounds(snapshot.lat, snapshot.lng, MAX_RADIUS_METERS);
        List<Task> local = taskDao.getActiveTasksInBoundingBox(now, bounds.minLat, bounds.maxLat, bounds.minLng, bounds.maxLng);

        Map<Long, Task> unique = new LinkedHashMap<>();
        for (Task task : general) {
            if (task.getLocationLat() == null || task.getLocationLng() == null) {
                unique.put(task.getId(), task);
            }
        }
        for (Task task : local) {
            unique.put(task.getId(), task);
        }
        return new ArrayList<>(unique.values());
    }

    private TaskContextMatch toEntity(long taskId, ContextSnapshot snapshot, long now, MatchResult result) {
        TaskContextMatch match = new TaskContextMatch();
        match.taskId = taskId;
        match.snapshotId = snapshot.id;
        match.matchedAt = now;
        match.relevanceScore = result.relevanceScore;
        match.matchReasons = join(result.reasons);
        match.shouldTriggerNow = result.shouldTriggerNow;
        match.cooldownUntil = result.cooldownUntil;
        match.triggerType = result.triggerType;
        match.contextSnapshotTrigger = snapshot.sourceTrigger;
        match.blockedBy = join(result.blockedBy);
        return match;
    }

    private static String join(List<String> items) {
        return items.isEmpty() ? "[]" : "[\"" + String.join("\",\"", items) + "\"]";
    }

    private static Bounds buildBounds(double lat, double lng, double radiusMeters) {
        double latDelta = radiusMeters / 111_320d;
        double lngDelta = radiusMeters / (111_320d * Math.cos(Math.toRadians(lat)));
        return new Bounds(lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta);
    }

    private static class Bounds {
        final double minLat;
        final double maxLat;
        final double minLng;
        final double maxLng;

        Bounds(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
        }
    }
}
