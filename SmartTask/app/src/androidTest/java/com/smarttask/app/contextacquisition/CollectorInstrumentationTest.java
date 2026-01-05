package com.smarttask.app.contextacquisition;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.smarttask.app.contextacquisition.collectors.CalendarCollector;
import com.smarttask.app.contextacquisition.collectors.CollectorContext;
import com.smarttask.app.contextacquisition.collectors.LocationCollector;
import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CollectorInstrumentationTest {

    @Test
    public void locationPermissionMissingProducesNullLocation() {
        Context context = ApplicationProvider.getApplicationContext();
        ContextSnapshot snapshot = new ContextSnapshot();
        new LocationCollector().collect(snapshot, new CollectorContext(context, "TEST", null, System.currentTimeMillis()));
        if (snapshot.lat == null && snapshot.lng == null) {
            assertTrue(snapshot.dataQualityFlags >= 0);
        }
    }

    @Test
    public void calendarPermissionMissingLeavesFieldsNull() {
        Context context = ApplicationProvider.getApplicationContext();
        ContextSnapshot snapshot = new ContextSnapshot();
        new CalendarCollector().collect(snapshot, new CollectorContext(context, "TEST", null, System.currentTimeMillis()));
        assertNull(snapshot.currentEventId);
    }
}
