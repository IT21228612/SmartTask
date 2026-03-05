package com.smarttask.app.contextmatching.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchResult {
    public final float relevanceScore;
    public final List<String> reasons;
    public final List<String> blockedBy;
    public final boolean shouldTriggerNow;
    public final String triggerType;
    @Nullable
    public final Long cooldownUntil;

    public MatchResult(float relevanceScore,
                       List<String> reasons,
                       List<String> blockedBy,
                       boolean shouldTriggerNow,
                       String triggerType,
                       @Nullable Long cooldownUntil) {
        this.relevanceScore = relevanceScore;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
        this.blockedBy = Collections.unmodifiableList(new ArrayList<>(blockedBy));
        this.shouldTriggerNow = shouldTriggerNow;
        this.triggerType = triggerType;
        this.cooldownUntil = cooldownUntil;
    }
}
