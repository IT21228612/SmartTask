package com.smarttask.app.contextacquisition.api;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.smarttask.app.contextacquisition.ContextDeriver;
import com.smarttask.app.contextacquisition.collectors.ActivityCollector;
import com.smarttask.app.contextacquisition.collectors.CalendarCollector;
import com.smarttask.app.contextacquisition.collectors.CollectorContext;
import com.smarttask.app.contextacquisition.collectors.ConnectivityCollector;
import com.smarttask.app.contextacquisition.collectors.ContextCollector;
import com.smarttask.app.contextacquisition.collectors.DeviceStateCollector;
import com.smarttask.app.contextacquisition.collectors.EnvironmentalCollector;
import com.smarttask.app.contextacquisition.collectors.GeofenceTriggerCollector;
import com.smarttask.app.contextacquisition.collectors.LocationCollector;
import com.smarttask.app.contextacquisition.collectors.PermissionStateCollector;
import com.smarttask.app.contextacquisition.collectors.TimeCollector;
import com.smarttask.app.contextacquisition.db.ContextDatabase;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.db.ContextSnapshotDao;
import com.smarttask.app.contextacquisition.geofence.TaskGeofenceSyncManager;
import com.smarttask.app.contextacquisition.utils.AppForegroundTracker;

import java.util.ArrayList;
import java.util.List;

public class ContextEngine {

    private static ContextEngine INSTANCE;
    private final Context appContext;
    private final ContextSnapshotDao dao;
    private final List<ContextCollector> collectors = new ArrayList<>();
    private final ContextDeriver deriver = new ContextDeriver();

    private ContextEngine(Context context) {
        this.appContext = context.getApplicationContext();
        this.dao = ContextDatabase.getInstance(appContext).contextSnapshotDao();
        initCollectors();
    }

    public static synchronized ContextEngine getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ContextEngine(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private void initCollectors() {
        collectors.add(new TimeCollector());
        collectors.add(new PermissionStateCollector());
        collectors.add(new GeofenceTriggerCollector());
        collectors.add(new LocationCollector());
        collectors.add(new ActivityCollector());
        collectors.add(new CalendarCollector());
        collectors.add(new DeviceStateCollector());
        collectors.add(new ConnectivityCollector());
        collectors.add(new EnvironmentalCollector());
    }

    public void captureSnapshot(String sourceTrigger, @Nullable Intent triggerIntent) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> captureSnapshotNowSync(sourceTrigger, triggerIntent));
    }

    public ContextSnapshot captureSnapshotNowSync(String sourceTrigger, @Nullable Intent triggerIntent) {
        long now = System.currentTimeMillis();
        CollectorContext collectorContext = new CollectorContext(appContext, sourceTrigger, triggerIntent, now);
        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.sourceTrigger = sourceTrigger;
        for (ContextCollector collector : collectors) {
            collector.collect(snapshot, collectorContext);
        }
        deriver.derive(snapshot);
        dao.insert(snapshot);
        return snapshot;
    }

    public ContextSnapshot getLatestSnapshot() {
        return dao.getLatest();
    }

    public void registerBackgroundCollectors() {
        AppForegroundTracker.init((android.app.Application) appContext);
        syncTaskGeofences();
    }

    public void syncTaskGeofences() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> new TaskGeofenceSyncManager(appContext).syncTaskGeofences());
    }
}
