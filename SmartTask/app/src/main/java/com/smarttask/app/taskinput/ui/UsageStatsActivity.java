package com.smarttask.app.taskinput.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.smarttask.app.R;
import com.smarttask.app.taskinput.db.TaskDao;
import com.smarttask.app.taskinput.db.TaskDatabase;
import com.smarttask.app.taskinput.db.UsageStatDao;
import com.smarttask.app.taskinput.db.UsageStatEvent;

import java.util.Locale;

public class UsageStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        TaskDatabase db = TaskDatabase.getInstance(this);
        TaskDao taskDao = db.taskDao();
        UsageStatDao usageStatDao = db.usageStatDao();

        int totalCreatedTasks = taskDao.getTotalTaskCount();
        int totalCompletedTasks = taskDao.getTotalCompletedTaskCount();
        int totalNotifications = usageStatDao.countByType(UsageStatEvent.EVENT_NOTIFICATION_SENT);
        int totalIgnoredNotifications = usageStatDao.countByType(UsageStatEvent.EVENT_NOTIFICATION_IGNORED);

        ((TextView) findViewById(R.id.stats_total_created_tasks)).setText(String.valueOf(totalCreatedTasks));
        ((TextView) findViewById(R.id.stats_total_completed_tasks)).setText(String.valueOf(totalCompletedTasks));
        ((TextView) findViewById(R.id.stats_total_notifications)).setText(String.valueOf(totalNotifications));
        ((TextView) findViewById(R.id.stats_total_ignored_notifications)).setText(String.valueOf(totalIgnoredNotifications));

        float completionRate = safeRate(totalCompletedTasks, totalCreatedTasks);
        float missedReminderRate = safeRate(totalIgnoredNotifications, totalNotifications);

        ((TextView) findViewById(R.id.stats_completion_rate)).setText(formatPercent(completionRate));
        ((TextView) findViewById(R.id.stats_missed_reminder_rate)).setText(formatPercent(missedReminderRate));
    }

    private static float safeRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0f;
        }
        return (numerator * 100f) / denominator;
    }

    private static String formatPercent(float value) {
        return String.format(Locale.US, "%.1f%%", value);
    }
}
