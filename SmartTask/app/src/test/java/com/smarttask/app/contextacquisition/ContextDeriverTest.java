package com.smarttask.app.contextacquisition;

import static org.junit.Assert.assertEquals;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;

import org.junit.Test;

public class ContextDeriverTest {

    @Test
    public void interruptionCostClamped() {
        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.isInMeeting = true;
        snapshot.activityType = "IN_VEHICLE";
        snapshot.doNotDisturbOn = true;
        snapshot.screenOn = false;
        new ContextDeriver().derive(snapshot);
        assertEquals(1.0f, snapshot.interruptionCostScore, 0.001f);
    }

    @Test
    public void receptivityScoreRules() {
        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.isInMeeting = false;
        snapshot.activityType = "STILL";
        snapshot.appInForeground = true;
        snapshot.screenOn = true;
        snapshot.deviceUnlocked = true;
        new ContextDeriver().derive(snapshot);
        assertEquals(0.8f, snapshot.receptivityScore, 0.001f);
    }

    @Test
    public void labelDefaults() {
        ContextSnapshot snapshot = new ContextSnapshot();
        snapshot.minuteOfDay = 1100;
        snapshot.screenOn = true;
        new ContextDeriver().derive(snapshot);
        assertEquals("EVENING_ACTIVE", snapshot.contextLabel);
    }
}
