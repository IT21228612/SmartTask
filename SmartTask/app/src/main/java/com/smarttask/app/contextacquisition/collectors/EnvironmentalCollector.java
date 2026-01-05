package com.smarttask.app.contextacquisition.collectors;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.HashUtils;
import com.smarttask.app.contextacquisition.utils.PrivacySettings;

public class EnvironmentalCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        Context app = ctx.appContext;
        AudioManager audioManager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            snapshot.headphonesConnected = hasHeadphones(audioManager);
            snapshot.bluetoothConnected = hasBluetoothAudio(audioManager);
        } else {
            Log.d(TAG, "cannot get headphonesConnected | reason : audio manager unavailable");
            Log.d(TAG, "cannot get bluetoothConnected | reason : audio manager unavailable");
        }
        collectWifiInfo(snapshot, ctx);
    }

    private boolean hasHeadphones(AudioManager audioManager) {
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo info : devices) {
            int type = info.getType();
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBluetoothAudio(AudioManager audioManager) {
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo info : devices) {
            if (info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || info.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                return true;
            }
        }
        return false;
    }

    private void collectWifiInfo(ContextSnapshot snapshot, CollectorContext ctx) {
        WifiManager wifiManager = (WifiManager) ctx.appContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.d(TAG, "cannot get wifiSsidHash | reason : wifi manager unavailable");
            return;
        }
        if (ContextCompat.checkSelfPermission(ctx.appContext, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "cannot get wifiSsidHash | reason : ACCESS_WIFI_STATE permission denied");
            return;
        }
        if (ContextCompat.checkSelfPermission(ctx.appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx.appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "cannot get wifiSsidHash | reason : location permission denied");
            return;
        }
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info == null) {
            Log.d(TAG, "cannot get wifiSsidHash | reason : wifi connection info unavailable");
            return;
        }
        String ssid = info.getSSID();
        PrivacySettings settings = new PrivacySettings(ctx.appContext);
        if (settings.hashWifiSsid()) {
            snapshot.wifiSsidHash = HashUtils.sha256(ssid);
            snapshot.anonymized = true;
        } else {
            snapshot.wifiSsidHash = ssid;
        }
    }
}
