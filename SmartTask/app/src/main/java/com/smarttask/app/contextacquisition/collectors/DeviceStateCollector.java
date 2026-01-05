package com.smarttask.app.contextacquisition.collectors;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.PowerManager;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.AppForegroundTracker;

public class DeviceStateCollector implements ContextCollector {
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        Context app = ctx.appContext;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = app.registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                snapshot.batteryPct = (int) ((level / (float) scale) * 100);
            }
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            snapshot.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        }

        PowerManager powerManager = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            snapshot.powerSaveMode = powerManager.isPowerSaveMode();
            snapshot.screenOn = powerManager.isInteractive();
        }

        AudioManager audioManager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int mode = audioManager.getRingerMode();
            if (mode == AudioManager.RINGER_MODE_SILENT) snapshot.ringerMode = "SILENT";
            else if (mode == AudioManager.RINGER_MODE_VIBRATE) snapshot.ringerMode = "VIBRATE";
            else snapshot.ringerMode = "NORMAL";
        }

        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            snapshot.doNotDisturbOn = nm.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL;
        }

        KeyguardManager km = (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE);
        if (km != null) {
            snapshot.deviceUnlocked = !km.isDeviceLocked();
        }
        snapshot.appInForeground = AppForegroundTracker.isForeground();
    }
}
