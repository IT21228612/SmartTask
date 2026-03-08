package com.smarttask.app.voiceCommandTaskCreation;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

public final class OpenAiManifestKeyReader {

    public static final String OPEN_AI_META_DATA_KEY = "com.smarttask.app.OPENAI_API_KEY";
    private static final String PLACEHOLDER_VALUE = "OPENAI_API_KEY_MISSING";

    private OpenAiManifestKeyReader() {
    }

    @Nullable
    public static String getOpenAiApiKey(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            Bundle metaData = applicationInfo.metaData;
            if (metaData == null) {
                return null;
            }
            String apiKey = metaData.getString(OPEN_AI_META_DATA_KEY);
            if (TextUtils.isEmpty(apiKey)) {
                return null;
            }
            String trimmed = apiKey.trim();
            if (PLACEHOLDER_VALUE.equals(trimmed) || trimmed.startsWith("${")) {
                return null;
            }
            return trimmed;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
