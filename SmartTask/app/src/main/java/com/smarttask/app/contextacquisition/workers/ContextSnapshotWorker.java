package com.smarttask.app.contextacquisition.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.smarttask.app.contextacquisition.api.ContextEngine;
import com.smarttask.app.contextacquisition.db.ContextDatabase;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ContextSnapshotWorker extends Worker {

    public static final String UNIQUE_NAME = "context_snapshot_periodic";
    private static final String UNIQUE_NAME_PREFIX = UNIQUE_NAME + "_";
    private static final String INPUT_CAPTURE_STARTED_AT_MS = "capture_started_at_ms";
    private static final int[] SNAPSHOT_MINUTES = {0, 15, 30, 45};

    public ContextSnapshotWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long captureStartedAtMs = getInputData().getLong(INPUT_CAPTURE_STARTED_AT_MS, System.currentTimeMillis());
        ContextEngine.getInstance(getApplicationContext())
                .captureSnapshotNowSync("PERIODIC", null, captureStartedAtMs);

        scheduleNextHourSlots(getApplicationContext(), captureStartedAtMs);

        long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        ContextDatabase.getInstance(getApplicationContext()).contextSnapshotDao().deleteOlderThan(cutoff);
        return Result.success();
    }

    public static void schedule(Context context) {
        scheduleNextHourSlots(context.getApplicationContext(), System.currentTimeMillis());
    }

    private static void scheduleNextHourSlots(Context context, long referenceTimeMs) {
        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());
        Calendar nextHour = Calendar.getInstance();
        nextHour.setTimeInMillis(referenceTimeMs);
        nextHour.set(Calendar.SECOND, 0);
        nextHour.set(Calendar.MILLISECOND, 0);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.add(Calendar.HOUR_OF_DAY, 1);

        for (int minute : SNAPSHOT_MINUTES) {
            Calendar slot = (Calendar) nextHour.clone();
            slot.set(Calendar.MINUTE, minute);
            scheduleSlotIfMissing(workManager, slot.getTimeInMillis());
        }
    }

    private static void scheduleSlotIfMissing(WorkManager workManager, long slotTimeMs) {
        String uniqueName = buildUniqueName(slotTimeMs);

        try {
            WorkInfo existing = workManager.getWorkInfosForUniqueWork(uniqueName).get()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (existing != null && existing.getState() != WorkInfo.State.CANCELLED
                    && existing.getState() != WorkInfo.State.FAILED) {
                return;
            }
        } catch (Exception ignored) {
            // If work state can't be read, still enqueue with KEEP so we don't duplicate.
        }

        long delayMs = Math.max(0L, slotTimeMs - System.currentTimeMillis());
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ContextSnapshotWorker.class)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder().putLong(INPUT_CAPTURE_STARTED_AT_MS, slotTimeMs).build())
                .setConstraints(new Constraints.Builder().build())
                .build();

        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.KEEP, request);
    }

    private static String buildUniqueName(long timeMs) {
        return String.format(Locale.US, "%s%d", UNIQUE_NAME_PREFIX, timeMs);
    }
}
