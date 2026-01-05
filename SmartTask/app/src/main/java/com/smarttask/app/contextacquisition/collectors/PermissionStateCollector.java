package com.smarttask.app.contextacquisition.collectors;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.DataQualityFlags;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class PermissionStateCollector implements ContextCollector {

    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        List<String> missing = new ArrayList<>();
        if (!PermissionUtils.hasLocationPermission(ctx.appContext)) {
            missing.add("LOCATION_DENIED");
            snapshot.dataQualityFlags |= DataQualityFlags.NO_LOCATION;
        }
        if (!PermissionUtils.hasActivityRecognition(ctx.appContext)) {
            missing.add("ACTIVITY_DENIED");
            snapshot.dataQualityFlags |= DataQualityFlags.NO_ACTIVITY;
        }
        if (!PermissionUtils.hasCalendar(ctx.appContext)) {
            missing.add("CALENDAR_DENIED");
            snapshot.dataQualityFlags |= DataQualityFlags.NO_CALENDAR;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(ctx.appContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                missing.add("BLUETOOTH_DENIED");
            }
        }
        snapshot.permissionState = missing.isEmpty() ? "OK" : String.join(",", missing);
    }
}
