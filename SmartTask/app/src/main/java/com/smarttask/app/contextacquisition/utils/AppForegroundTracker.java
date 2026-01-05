package com.smarttask.app.contextacquisition.utils;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

public final class AppForegroundTracker implements LifecycleEventObserver {

    private static boolean foreground = false;
    private static AppForegroundTracker INSTANCE;

    private AppForegroundTracker() {
    }

    public static void init(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new AppForegroundTracker();
            ProcessLifecycleOwner.get().getLifecycle().addObserver(INSTANCE);
        }
    }

    public static boolean isForeground() {
        return foreground;
    }

    @Override
    public void onStateChanged(androidx.lifecycle.LifecycleOwner source, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_START) {
            foreground = true;
        } else if (event == Lifecycle.Event.ON_STOP) {
            foreground = false;
        }
    }
}
