package com.smarttask.app.prioritization.logic;

public class PriorityScoreBreakdown {
    public final float urgencyScore;
    public final float contextScore;
    public final float interruptionPenalty;
    public final float manualBoost;
    public final float finalScore;

    public PriorityScoreBreakdown(float urgencyScore,
                                  float contextScore,
                                  float interruptionPenalty,
                                  float manualBoost,
                                  float finalScore) {
        this.urgencyScore = urgencyScore;
        this.contextScore = contextScore;
        this.interruptionPenalty = interruptionPenalty;
        this.manualBoost = manualBoost;
        this.finalScore = finalScore;
    }
}
