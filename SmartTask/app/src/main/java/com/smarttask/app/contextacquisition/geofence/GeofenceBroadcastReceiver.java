package com.smarttask.app.contextacquisition.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smarttask.app.contextacquisition.api.ContextEngine;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ContextEngine.getInstance(context).captureSnapshot("GEOFENCE", intent);
    }
}
