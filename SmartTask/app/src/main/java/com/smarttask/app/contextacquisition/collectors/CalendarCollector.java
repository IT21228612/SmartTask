package com.smarttask.app.contextacquisition.collectors;

import android.Manifest;
import android.content.Context;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;
import com.smarttask.app.contextacquisition.utils.HashUtils;
import com.smarttask.app.contextacquisition.utils.PermissionUtils;
import com.smarttask.app.contextacquisition.utils.PrivacySettings;

import java.util.concurrent.TimeUnit;

public class CalendarCollector implements ContextCollector {

    private static final String TAG = "contextCollector";
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        if (!PermissionUtils.hasCalendar(ctx.appContext)) {
            snapshot.permissionState = appendState(snapshot.permissionState, "CALENDAR_DENIED");
            Log.d(TAG, "cannot get isInMeeting | reason : calendar permission denied");
            Log.d(TAG, "cannot get currentEventId | reason : calendar permission denied");
            Log.d(TAG, "cannot get currentEventTitle | reason : calendar permission denied");
            Log.d(TAG, "cannot get eventBusyStatus | reason : calendar permission denied");
            Log.d(TAG, "cannot get minutesLeftInEvent | reason : calendar permission denied");
            Log.d(TAG, "cannot get minutesToNextEvent | reason : calendar permission denied");
            return;
        }
        long now = ctx.now;
        long end = now + TimeUnit.DAYS.toMillis(1);
        queryCalendar(ctx.appContext, snapshot, now, end);
    }

    private void queryCalendar(Context context, ContextSnapshot snapshot, long start, long end) {
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start - TimeUnit.HOURS.toMillis(12));
        ContentUris.appendId(builder, end);


        String[] projection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.AVAILABILITY
        };
        Cursor cursor = context.getContentResolver().query(builder.build(), projection, null, null, CalendarContract.Instances.BEGIN + " ASC");
        if (cursor == null) {
            Log.d(TAG, "cannot get isInMeeting | reason : calendar query returned null cursor");
            Log.d(TAG, "cannot get currentEventId | reason : calendar query returned null cursor");
            Log.d(TAG, "cannot get currentEventTitle | reason : calendar query returned null cursor");
            Log.d(TAG, "cannot get eventBusyStatus | reason : calendar query returned null cursor");
            Log.d(TAG, "cannot get minutesLeftInEvent | reason : calendar query returned null cursor");
            Log.d(TAG, "cannot get minutesToNextEvent | reason : calendar query returned null cursor");
            return;
        }
        PrivacySettings settings = new PrivacySettings(context);
        try {
            long nextStart = Long.MAX_VALUE;
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(0);
                String title = cursor.getString(1);
                long begin = cursor.getLong(2);
                long endTime = cursor.getLong(3);
                int availability = cursor.getInt(4);
                boolean busy = availability == CalendarContract.Instances.AVAILABILITY_BUSY;
                boolean active = start >= begin && start <= endTime && busy;
                if (active) {
                    snapshot.isInMeeting = true;
                    snapshot.currentEventId = String.valueOf(eventId);
                    snapshot.currentEventTitle = settings.storeCalendarTitles() ? title : HashUtils.sha256(title);
                    snapshot.eventBusyStatus = "BUSY";
                    snapshot.minutesLeftInEvent = (int) ((endTime - start) / 60000);
                } else if (begin > start && begin < nextStart && busy) {
                    nextStart = begin;
                    snapshot.minutesToNextEvent = (int) ((begin - start) / 60000);
                }
            }
        } finally {
            cursor.close();
        }
    }

    private String appendState(String current, String state) {
        if (TextUtils.isEmpty(current) || "OK".equals(current)) {
            return state;
        }
        if (current.contains(state)) return current;
        return current + "," + state;
    }
}
