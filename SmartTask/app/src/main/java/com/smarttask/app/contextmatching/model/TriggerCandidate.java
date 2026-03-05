package com.smarttask.app.contextmatching.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TriggerCandidate {
    public final long taskId;
    public final long snapshotId;
    public final float relevanceScore;
    public final List<String> reasons;
    public final List<String> blockedBy;

    public TriggerCandidate(long taskId, long snapshotId, float relevanceScore, List<String> reasons, List<String> blockedBy) {
        this.taskId = taskId;
        this.snapshotId = snapshotId;
        this.relevanceScore = relevanceScore;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
        this.blockedBy = Collections.unmodifiableList(new ArrayList<>(blockedBy));
    }
}
