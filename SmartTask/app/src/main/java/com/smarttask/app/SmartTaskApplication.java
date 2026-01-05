package com.smarttask.app;

import android.app.Application;

import com.smarttask.app.contextacquisition.api.ContextEngine;
import com.smarttask.app.contextacquisition.workers.ContextSnapshotWorker;

public class SmartTaskApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextEngine contextEngine = ContextEngine.getInstance(this);
        contextEngine.registerBackgroundCollectors();
        contextEngine.captureSnapshot("APP_START", null);
        ContextSnapshotWorker.schedule(this);
    }
}
