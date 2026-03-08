package com.smarttask.app.voiceCommandTaskCreation;

import androidx.annotation.Nullable;

public class ParsedVoiceTask {

    @Nullable
    private String taskTitle;
    @Nullable
    private String description;
    @Nullable
    private String category;
    @Nullable
    private String priority;
    @Nullable
    private String dueDatetime;
    @Nullable
    private String preferredStartDatetime;
    @Nullable
    private String preferredEndDatetime;
    @Nullable
    private Integer locationRadiusMeters;
    @Nullable
    private Boolean enableNotifications;
    private String rawTranscript;
    @Nullable
    private String extractedJson;

    public ParsedVoiceTask(String rawTranscript) {
        this.rawTranscript = rawTranscript;
    }

    @Nullable
    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(@Nullable String taskTitle) {
        this.taskTitle = taskTitle;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    @Nullable
    public String getPriority() {
        return priority;
    }

    public void setPriority(@Nullable String priority) {
        this.priority = priority;
    }

    @Nullable
    public String getDueDatetime() {
        return dueDatetime;
    }

    public void setDueDatetime(@Nullable String dueDatetime) {
        this.dueDatetime = dueDatetime;
    }

    @Nullable
    public String getPreferredStartDatetime() {
        return preferredStartDatetime;
    }

    public void setPreferredStartDatetime(@Nullable String preferredStartDatetime) {
        this.preferredStartDatetime = preferredStartDatetime;
    }

    @Nullable
    public String getPreferredEndDatetime() {
        return preferredEndDatetime;
    }

    public void setPreferredEndDatetime(@Nullable String preferredEndDatetime) {
        this.preferredEndDatetime = preferredEndDatetime;
    }

    @Nullable
    public Integer getLocationRadiusMeters() {
        return locationRadiusMeters;
    }

    public void setLocationRadiusMeters(@Nullable Integer locationRadiusMeters) {
        this.locationRadiusMeters = locationRadiusMeters;
    }

    @Nullable
    public Boolean getEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(@Nullable Boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public String getRawTranscript() {
        return rawTranscript;
    }

    public void setRawTranscript(String rawTranscript) {
        this.rawTranscript = rawTranscript;
    }

    @Nullable
    public String getExtractedJson() {
        return extractedJson;
    }

    public void setExtractedJson(@Nullable String extractedJson) {
        this.extractedJson = extractedJson;
    }
}

