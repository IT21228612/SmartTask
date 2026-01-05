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
    public long id;

    // Identity & timing
    public long timestamp = 0L;
    @NonNull
    public String timezoneId = "";
    public int dayOfWeek = 0;
    public int minuteOfDay = 0;

    // Location
    @Nullable
    public Double lat;
    @Nullable
    public Double lng;
    @Nullable
    public Float accuracyM;
    @Nullable
    public Float speedMps;
    @Nullable
    public Float bearingDeg;
    public boolean isGeofenceHit = false;
    @Nullable
    public String geofenceId;
    @Nullable
    public String placeLabel;
    @NonNull
    public String locationSource = "UNKNOWN";

    // Activity/motion
    @Nullable
    public String activityType;
    public int activityConfidence = 0;
    public boolean isMoving = false;
    @Nullable
    public Integer stepsSinceLast;

    // Calendar
    public boolean isInMeeting = false;
    @Nullable
    public String currentEventId;
    @Nullable
    public String currentEventTitle;
    @Nullable
    public String eventBusyStatus;
    @Nullable
    public Integer minutesToNextEvent;
    @Nullable
    public Integer minutesLeftInEvent;

    // Device state
    public int batteryPct = 0;
    public boolean isCharging = false;
    public boolean powerSaveMode = false;
    @Nullable
    public String ringerMode;
    public boolean doNotDisturbOn = false;
    public boolean screenOn = false;
    public boolean deviceUnlocked = false;
    public boolean appInForeground = false;

    // Connectivity
    @Nullable
    public String connectivityType;
    public boolean isInternetAvailable = false;
    @Nullable
    public Boolean isRoaming;

    // Environmental
    @Nullable
    public Boolean headphonesConnected;
    @Nullable
    public Boolean bluetoothConnected;
    @Nullable
    public String wifiSsidHash;
    @Nullable
    public Float noiseLevelDb;

    // Derived scoring helpers
    public float interruptionCostScore = 0f;
    public float receptivityScore = 0f;
    @Nullable
    public String contextLabel;

    // Privacy/logging
    @NonNull
    public String permissionState = "OK";
    public int dataQualityFlags = 0;
    public boolean anonymized = false;
    @NonNull
    public String sourceTrigger = "UNKNOWN";
}
