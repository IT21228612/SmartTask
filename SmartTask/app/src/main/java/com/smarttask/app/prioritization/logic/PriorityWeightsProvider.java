package com.smarttask.app.prioritization.logic;

public class PriorityWeightsProvider {

    public static final float URGENCY_WEIGHT = 0.50f;
    public static final float CONTEXT_WEIGHT = 0.35f;
    public static final float INTERRUPTION_WEIGHT = 0.15f;
    public static final float ANTI_JITTER_THRESHOLD = 5f;

    private PriorityWeightsProvider() {
        // utility
    }
}
