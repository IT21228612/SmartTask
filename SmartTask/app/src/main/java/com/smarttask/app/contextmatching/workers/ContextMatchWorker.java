package com.smarttask.app.contextmatching.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.smarttask.app.contextmatching.logic.ContextMatchingRunner;

public class ContextMatchWorker extends Worker {

    private static final String UNIQUE_PREFIX = "context_match_";
    public static final String INPUT_SNAPSHOT_ID = "input_snapshot_id";

    public ContextMatchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long snapshotId = getInputData().getLong(INPUT_SNAPSHOT_ID, -1L);
        if (snapshotId <= 0L) {
            return Result.failure();
        }

        new ContextMatchingRunner(getApplicationContext()).runForSnapshot(getApplicationContext(), snapshotId);
        return Result.success();
    }

    public static void enqueue(@NonNull Context context, long snapshotId) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ContextMatchWorker.class)
                .setInputData(new androidx.work.Data.Builder().putLong(INPUT_SNAPSHOT_ID, snapshotId).build())
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(UNIQUE_PREFIX + snapshotId, ExistingWorkPolicy.REPLACE, request);
    }
}
