package com.smarttask.app.contextacquisition.collectors;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeCollector implements ContextCollector {
    @Override
    public void collect(ContextSnapshot snapshot, CollectorContext ctx) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ctx.now);
        snapshot.timestamp = ctx.now;
        TimeZone tz = calendar.getTimeZone();
        snapshot.timezoneId = tz.getID();
        snapshot.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        snapshot.minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }
}
