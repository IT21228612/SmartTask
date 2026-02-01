package com.smarttask.app.contextacquisition.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "context_snapshots",
        indices = {
                @Index("timestamp"),
                @Index("sourceTrigger"),
                @Index("geofenceId")
        }
)
public class ContextSnapshot {

    @PrimaryKey(autoGenerate = true)
    public long id; // set by Room auto-generate (database insert)

    // Identity & timing
    public long timestamp = 0L; // set by TimeCollector
    @NonNull
    public String timezoneId = ""; // set by TimeCollector
    public int dayOfWeek = 0; // set by TimeCollector
    public int minuteOfDay = 0; // set by TimeCollector

    // Location
    @Nullable
    public Double lat; // set by LocationCollector
    @Nullable
    public Double lng; // set by LocationCollector
    @Nullable
    public Float accuracyM; // set by LocationCollector
    @Nullable
    public Float speedMps; // set by LocationCollector
    @Nullable
    public Float bearingDeg; // set by LocationCollector
    public boolean isGeofenceHit = false; // set by GeofenceTriggerCollector
    @Nullable
    public String geofenceId; // set by GeofenceTriggerCollector
    @Nullable
    public String placeLabel; // set by GeofenceTriggerCollector
    @NonNull
    public String locationSource = "UNKNOWN"; // set by LocationCollector

    // Activity/motion
    @Nullable
    public String activityType; // set by ActivityCollector
    public int activityConfidence = 0; // set by ActivityCollector
    public boolean isMoving = false; // set by ActivityCollector
    @Nullable
    public Integer stepsSinceLast; // not set by any collector yet

    // Calendar
    public boolean isInMeeting = false; // set by CalendarCollector
    @Nullable
    public String currentEventId; // set by CalendarCollector
    @Nullable
    public String currentEventTitle; // set by CalendarCollector
    @Nullable
    public String eventBusyStatus; // set by CalendarCollector
    @Nullable
    public Integer minutesToNextEvent; // set by CalendarCollector
    @Nullable
    public Integer minutesLeftInEvent; // set by CalendarCollector

    // Device state
    public int batteryPct = 0; // set by DeviceStateCollector
    public boolean isCharging = false; // set by DeviceStateCollector
    public boolean powerSaveMode = false; // set by DeviceStateCollector
    @Nullable
    public String ringerMode; // set by DeviceStateCollector
    public boolean doNotDisturbOn = false; // set by DeviceStateCollector
    public boolean screenOn = false; // set by DeviceStateCollector
    public boolean deviceUnlocked = false; // set by DeviceStateCollector
    public boolean appInForeground = false; // set by DeviceStateCollector

    // Connectivity
    @Nullable
    public String connectivityType; // set by ConnectivityCollector
    public boolean isInternetAvailable = false; // set by ConnectivityCollector
    @Nullable
    public Boolean isRoaming; // set by ConnectivityCollector

    // Environmental
    @Nullable
    public Boolean headphonesConnected; // set by EnvironmentalCollector
    @Nullable
    public Boolean bluetoothConnected; // set by EnvironmentalCollector
    @Nullable
    public String wifiSsidHash; // set by EnvironmentalCollector
    @Nullable
    public Float noiseLevelDb; // not set by any collector yet

    // Derived scoring helpers
    public float interruptionCostScore = 0f; // set by ContextDeriver
    public float receptivityScore = 0f; // set by ContextDeriver
    @Nullable
    public String contextLabel; // set by ContextDeriver

    // Privacy/logging
    @NonNull
    public String permissionState = "OK"; // set by PermissionStateCollector (also appended by ActivityCollector/CalendarCollector)
    public int dataQualityFlags = 0; // set by PermissionStateCollector/LocationCollector/ConnectivityCollector
    public boolean anonymized = false; // set by EnvironmentalCollector
    @NonNull
    public String sourceTrigger = "UNKNOWN"; // set by ContextEngine
}
