package com.smarttask.app.contextacquisition.collectors;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

public class CollectorContext {
    public final Context appContext;
    public final String sourceTrigger;
    @Nullable
    public final Intent triggerIntent;
    public final long now;

    public CollectorContext(Context appContext, String sourceTrigger, @Nullable Intent triggerIntent, long now) {
        this.appContext = appContext;
        this.sourceTrigger = sourceTrigger;
        this.triggerIntent = triggerIntent;
        this.now = now;
    }
}
