package com.smarttask.app.taskinput.db;

import android.content.Context;

import androidx.annotation.NonNull;

public final class UsageStatsTracker {

    private UsageStatsTracker() {
    }

    public static void logTaskCreated(@NonNull Context context, long taskId) {
        log(context, UsageStatEvent.EVENT_TASK_CREATED, taskId, null);
    }

    public static void logTaskCompleted(@NonNull Context context, long taskId) {
        log(context, UsageStatEvent.EVENT_TASK_COMPLETED, taskId, null);
    }

    public static void logNotificationSent(@NonNull Context context, long taskId, int notificationId) {
        log(context, UsageStatEvent.EVENT_NOTIFICATION_SENT, taskId, notificationId);
    }

    public static void logNotificationActioned(@NonNull Context context, long taskId, int notificationId) {
        log(context, UsageStatEvent.EVENT_NOTIFICATION_ACTIONED, taskId, notificationId);
    }

    public static void logNotificationIgnoredIfNeeded(@NonNull Context context, long taskId, int notificationId) {
        TaskDatabase database = TaskDatabase.getInstance(context);
        UsageStatDao dao = database.usageStatDao();
        if (dao.hasEventForNotification(UsageStatEvent.EVENT_NOTIFICATION_ACTIONED, notificationId)) {
            return;
        }
        if (dao.hasEventForNotification(UsageStatEvent.EVENT_NOTIFICATION_IGNORED, notificationId)) {
            return;
        }
        dao.insert(new UsageStatEvent(UsageStatEvent.EVENT_NOTIFICATION_IGNORED, taskId, notificationId, System.currentTimeMillis()));
    }

    private static void log(@NonNull Context context,
                            @NonNull String eventType,
                            long taskId,
                            Integer notificationId) {
        TaskDatabase.getInstance(context).usageStatDao().insert(
                new UsageStatEvent(eventType, taskId, notificationId, System.currentTimeMillis())
        );
    }
}
