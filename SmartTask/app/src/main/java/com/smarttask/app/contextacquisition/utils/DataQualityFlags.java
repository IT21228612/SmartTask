package com.smarttask.app.contextacquisition.utils;

public final class DataQualityFlags {
    private DataQualityFlags() {
    }

    public static final int LOW_ACCURACY = 1 << 0;
    public static final int STALE_LOCATION = 1 << 1;
    public static final int NO_LOCATION = 1 << 2;
    public static final int NO_ACTIVITY = 1 << 3;
    public static final int NO_CALENDAR = 1 << 4;
    public static final int NO_CONNECTIVITY_INFO = 1 << 5;
}
