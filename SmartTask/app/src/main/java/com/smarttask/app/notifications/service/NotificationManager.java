package com.smarttask.app.notifications.service;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.smarttask.app.R;
import com.smarttask.app.contextmatching.model.TriggerCandidate;
import com.smarttask.app.prioritization.logic.PrioritizedTask;
import com.smarttask.app.taskinput.ui.TaskListActivity;

import java.util.List;
import java.util.Locale;

public class NotificationManager {

    public static final String CHANNEL_ID = "smarttask_reminders";
    public static final String ACTION_DONE = "com.smarttask.app.notifications.ACTION_DONE";
    public static final String ACTION_SNOOZE = "com.smarttask.app.notifications.ACTION_SNOOZE";
    public static final String ACTION_POSTPONE = "com.smarttask.app.notifications.ACTION_POSTPONE";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    private final Context appContext;
    private final NotificationDecisionEngine decisionEngine;

    public NotificationManager(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.decisionEngine = new NotificationDecisionEngine();
    }

    public void dispatch(long snapshotId,
                         @NonNull List<TriggerCandidate> triggerCandidates,
                         @NonNull List<PrioritizedTask> prioritizedTasks) {
        createChannelIfNeeded(appContext);
        List<NotificationDecisionEngine.Decision> decisions =
                decisionEngine.selectNotificationDecisions(prioritizedTasks, triggerCandidates);
        for (NotificationDecisionEngine.Decision decision : decisions) {
            showTaskNotification(snapshotId, decision);
        }
    }

    public static void createChannelIfNeeded(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null || notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        android.app.NotificationChannel channel = new android.app.NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                android.app.NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    private void showTaskNotification(long snapshotId, @NonNull NotificationDecisionEngine.Decision decision) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        long taskId = decision.prioritizedTask.task.getId();
        int notificationId = (int) (taskId % Integer.MAX_VALUE);

        Intent openIntent = new Intent(appContext, TaskListActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra("extra_snapshot_id", snapshotId);

        PendingIntent contentIntent = PendingIntent.getActivity(
                appContext,
                buildRequestCode(taskId, 10),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String reasonSummary = decision.triggerCandidate.reasons.isEmpty()
                ? appContext.getString(R.string.notification_reason_default)
                : String.join(" • ", decision.triggerCandidate.reasons);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(decision.prioritizedTask.task.getTitle())
                .setContentText(appContext.getString(
                        R.string.notification_content_template,
                        formatScore(decision.prioritizedTask.breakdown.finalScore),
                        reasonSummary
                ))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(appContext.getString(
                        R.string.notification_big_text_template,
                        formatScore(decision.prioritizedTask.breakdown.finalScore),
                        formatScore(decision.triggerCandidate.relevanceScore),
                        reasonSummary
                )))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .addAction(0, appContext.getString(R.string.notification_action_done),
                        buildActionPendingIntent(taskId, notificationId, ACTION_DONE, 20))
                .addAction(0, appContext.getString(R.string.notification_action_snooze),
                        buildActionPendingIntent(taskId, notificationId, ACTION_SNOOZE, 30))
                .addAction(0, appContext.getString(R.string.notification_action_postpone),
                        buildActionPendingIntent(taskId, notificationId, ACTION_POSTPONE, 40));

        NotificationManagerCompat.from(appContext).notify(notificationId, builder.build());
    }

    private PendingIntent buildActionPendingIntent(long taskId,
                                                   int notificationId,
                                                   @NonNull String action,
                                                   int actionSalt) {
        Intent actionIntent = new Intent(appContext, NotificationActionReceiver.class)
                .setAction(action)
                .putExtra(EXTRA_TASK_ID, taskId)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(
                appContext,
                buildRequestCode(taskId, actionSalt),
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static int buildRequestCode(long taskId, int salt) {
        return (int) ((taskId % 1_000_000L) + salt * 1_000_000L);
    }

    private static String formatScore(float score) {
        return String.format(Locale.US, "%.1f", score);
    }
}
