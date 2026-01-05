package com.smarttask.app.contextacquisition.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.smarttask.app.contextacquisition.api.ContextEngine;
import com.smarttask.app.contextacquisition.db.ContextDatabase;

import java.util.concurrent.TimeUnit;

public class ContextSnapshotWorker extends Worker {

    public static final String UNIQUE_NAME = "context_snapshot_periodic";

    public ContextSnapshotWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        ContextEngine.getInstance(getApplicationContext()).captureSnapshotNowSync("PERIODIC", null);
        long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        ContextDatabase.getInstance(getApplicationContext()).contextSnapshotDao().deleteOlderThan(cutoff);
        return Result.success();
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ContextSnapshotWorker.class,
                15,
                TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder().build())
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
