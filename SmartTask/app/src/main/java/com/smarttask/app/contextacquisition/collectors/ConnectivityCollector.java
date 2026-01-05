package com.smarttask.app.contextacquisition.collectors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.DataQualityFlags;

public class ConnectivityCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            snapshot.dataQualityFlags |= DataQualityFlags.NO_CONNECTIVITY_INFO;
            Log.d(TAG, "cannot get connectivityType | reason : connectivity manager unavailable");
            Log.d(TAG, "cannot get isInternetAvailable | reason : connectivity manager unavailable");
            Log.d(TAG, "cannot get isRoaming | reason : connectivity manager unavailable");
            return;
        }
        Network network = cm.getActiveNetwork();
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities == null) {
            snapshot.connectivityType = "NONE";
            Log.d(TAG, "cannot get connectivityType | reason : no active network capabilities");
            Log.d(TAG, "cannot get isInternetAvailable | reason : no active network capabilities");
            Log.d(TAG, "cannot get isRoaming | reason : no active network capabilities");
            return;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            snapshot.connectivityType = "WIFI";
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            snapshot.connectivityType = "CELLULAR";
        } else {
            snapshot.connectivityType = "NONE";
        }
        snapshot.isInternetAvailable = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        TelephonyManager tm = (TelephonyManager) ctx.appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            snapshot.isRoaming = tm.isNetworkRoaming();
        } else {
            Log.d(TAG, "cannot get isRoaming | reason : telephony manager unavailable");
        }
    }
}
