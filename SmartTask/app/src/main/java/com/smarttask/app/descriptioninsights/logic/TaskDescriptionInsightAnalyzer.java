package com.smarttask.app.descriptioninsights.logic;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smarttask.app.taskinput.db.Task;
import com.smarttask.app.voiceCommandTaskCreation.OpenAiManifestKeyReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TaskDescriptionInsightAnalyzer {
    public static final String MODEL = "gpt-4.1-mini";
    private static final String RESPONSES_API_URL = "https://api.openai.com/v1/responses";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Nullable
    public AnalysisResult analyze(@NonNull Context context, @NonNull Task task) {
        String description = task.getDescription() == null ? "" : task.getDescription().trim();
        if (description.isEmpty()) return null;
        String apiKey = OpenAiManifestKeyReader.getOpenAiApiKey(context);
        if (TextUtils.isEmpty(apiKey)) return null;

        try {
            JSONObject requestJson = buildRequestJson(task);
            Request request = new Request.Builder()
                    .url(RESPONSES_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestJson.toString(), JSON))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return null;
                String body = response.body().string();
                JSONObject parsed = new JSONObject(extractStructuredJson(new JSONObject(body)));
                AnalysisResult result = new AnalysisResult();
                result.riskIfDelayed = clamp((float) parsed.optDouble("risk_if_delayed", 0d), 0f, 100f);
                result.requiresDeepFocus = parsed.optBoolean("requires_deep_focus", false);
                result.dependencyBlocked = parsed.optBoolean("dependency_blocked", false);
                int duration = parsed.optInt("suggested_duration_min", -1);
                result.suggestedDurationMin = duration > 0 ? duration : null;
                result.rawJson = parsed.toString();
                result.model = MODEL;
                result.descriptionHash = sha256(description);
                return result;
            }
        } catch (IOException | JSONException ignored) {
            return null;
        }
    }

    private JSONObject buildRequestJson(Task task) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("model", MODEL);

        JSONArray input = new JSONArray();
        input.put(new JSONObject().put("role", "system").put("content",
                "Analyze a task description for prioritization. Return strict JSON only."));
        input.put(new JSONObject().put("role", "user").put("content",
                "Title: " + task.getTitle() + "\nDescription: " + (task.getDescription() == null ? "" : task.getDescription())));
        request.put("input", input);

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        JSONObject props = new JSONObject();
        props.put("risk_if_delayed", new JSONObject().put("type", "number"));
        props.put("requires_deep_focus", new JSONObject().put("type", "boolean"));
        props.put("dependency_blocked", new JSONObject().put("type", "boolean"));
        props.put("suggested_duration_min", new JSONObject().put("type", "integer"));
        schema.put("properties", props);
        schema.put("required", new JSONArray()
                .put("risk_if_delayed")
                .put("requires_deep_focus")
                .put("dependency_blocked")
                .put("suggested_duration_min"));

        request.put("text", new JSONObject().put("format", new JSONObject()
                .put("type", "json_schema")
                .put("name", "task_description_insights")
                .put("strict", true)
                .put("schema", schema)));
        return request;
    }

    private String extractStructuredJson(JSONObject root) {
        String outputText = root.optString("output_text", null);
        if (!TextUtils.isEmpty(outputText)) return outputText;
        JSONArray output = root.optJSONArray("output");
        if (output == null || output.length() == 0) return "{}";
        JSONObject item = output.optJSONObject(0);
        if (item == null) return "{}";
        JSONArray content = item.optJSONArray("content");
        if (content == null || content.length() == 0) return "{}";
        JSONObject contentItem = content.optJSONObject(0);
        return contentItem == null ? "{}" : contentItem.optString("text", "{}");
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) sb.append(String.format(Locale.US, "%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    public static class AnalysisResult {
        public float riskIfDelayed;
        public boolean requiresDeepFocus;
        public boolean dependencyBlocked;
        @Nullable public Integer suggestedDurationMin;
        public String descriptionHash;
        public String rawJson;
        public String model;
    }
}
