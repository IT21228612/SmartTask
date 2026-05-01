package com.smarttask.app.notifications.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;
import com.smarttask.app.taskinput.db.UsageStatsTracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION_DISMISSED = "com.smarttask.app.notifications.ACTION_DISMISSED";

    private static final long SNOOZE_MS = 60 * 60 * 1000L;
    private static final long POSTPONE_MS = 24 * 60 * 60 * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        long taskId = intent.getLongExtra(NotificationManager.EXTRA_TASK_ID, -1L);
        int notificationId = intent.getIntExtra(NotificationManager.EXTRA_NOTIFICATION_ID, -1);
        if (taskId <= 0L) {
            return;
        }

        Context appContext = context.getApplicationContext();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> handleAction(appContext, intent.getAction(), taskId, notificationId));
        executor.shutdown();

        if (notificationId >= 0 && !ACTION_NOTIFICATION_DISMISSED.equals(intent.getAction())) {
            NotificationManagerCompat.from(appContext).cancel(notificationId);
        }
    }

    private void handleAction(@NonNull Context context, @NonNull String action, long taskId, int notificationId) {
        if (ACTION_NOTIFICATION_DISMISSED.equals(action)) {
            if (notificationId >= 0) {
                UsageStatsTracker.logNotificationIgnoredIfNeeded(context, taskId, notificationId);
            }
            return;
        }

        TaskDao taskDao = TaskDatabase.getInstance(context).taskDao();
        Task task = taskDao.getTaskById(taskId);
        if (task == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (NotificationManager.ACTION_DONE.equals(action)) {
            task.setCompleted(true);
            task.setCompletedAt(now);
            task.setSnoozeUntil(null);
        } else if (NotificationManager.ACTION_SNOOZE.equals(action)) {
            task.setSnoozeUntil(now + SNOOZE_MS);
            task.setCompleted(false);
            task.setCompletedAt(null);
        } else if (NotificationManager.ACTION_POSTPONE.equals(action)) {
            Long dueAt = task.getDueAt();
            long base = dueAt != null ? Math.max(dueAt, now) : now;
            task.setDueAt(base + POSTPONE_MS);
            task.setSnoozeUntil(null);
        } else {
            return;
        }

        task.setUpdatedAt(now);
        taskDao.updateTask(task);
        if (notificationId >= 0) {
            UsageStatsTracker.logNotificationActioned(context, taskId, notificationId);
        }
        if (NotificationManager.ACTION_DONE.equals(action)) {
            UsageStatsTracker.logTaskCompleted(context, taskId);
        }
    }
}
