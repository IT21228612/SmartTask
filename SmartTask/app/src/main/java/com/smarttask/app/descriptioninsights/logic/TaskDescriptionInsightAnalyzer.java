package com.smarttask.app.descriptioninsights.logic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
    private static final String TAG = "DescInsightAnalyzer";

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Nullable
    public AnalysisResult analyze(@NonNull Context context, @NonNull Task task) {
        String description = task.getDescription() == null ? "" : task.getDescription().trim();
        if (description.isEmpty()) {
            Log.w(TAG, "Skipping OpenAI call: empty description for taskId=" + task.getId());
            return null;
        }
        String apiKey = OpenAiManifestKeyReader.getOpenAiApiKey(context);
        if (TextUtils.isEmpty(apiKey)) {
            Log.e(TAG, "Skipping OpenAI call: missing API key for taskId=" + task.getId());
            return null;
        }

        try {
            JSONObject requestJson = buildRequestJson(task);
            Log.d(TAG, "Calling OpenAI responses API for taskId=" + task.getId() + ", model=" + MODEL);
            Request request = new Request.Builder()
                    .url(RESPONSES_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestJson.toString(), JSON))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    Log.e(TAG, "OpenAI call failed for taskId=" + task.getId()
                            + ", code=" + response.code()
                            + ", message=" + response.message()
                            + ", body=" + errorBody);
                    return null;
                }
                if (response.body() == null) {
                    Log.e(TAG, "OpenAI call failed: empty response body for taskId=" + task.getId());
                    return null;
                }
                String body = response.body().string();
                JSONObject parsed = new JSONObject(extractStructuredJson(new JSONObject(body)));
                AnalysisResult result = new AnalysisResult();
                result.riskIfDelayed = clamp((float) parsed.optDouble("risk_if_delayed", 0d), 0f, 100f);
                result.requiresDeepFocus = parsed.optBoolean("requires_deep_focus", false);
                int duration = parsed.optInt("suggested_duration_min", -1);
                result.suggestedDurationMin = duration > 0 ? duration : null;
                result.complexityLevel = clamp(parsed.optInt("complexity_level", 0), 0, 5);
                result.energyDemand = clamp(parsed.optInt("energy_demand", 0), 0, 5);
                result.relationshipValue = clamp(parsed.optInt("relationship_value", 0), 0, 5);
                result.rawJson = parsed.toString();
                result.model = MODEL;
                result.descriptionHash = sha256(description);
                Log.d(TAG, "OpenAI analysis succeeded for taskId=" + task.getId());
                return result;
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "OpenAI analysis exception for taskId=" + task.getId(), e);
            return null;
        }
    }

    private JSONObject buildRequestJson(Task task) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("model", MODEL);

        JSONArray input = new JSONArray();
        input.put(new JSONObject().put("role", "system").put("content",
                "Analyze task title and description for prioritization.\n"
                        + "Return strict JSON only.\n"
                        + "Field meanings:\n"
                        + "- risk_if_delayed: 0-100, impact if delayed.\n"
                        + "- requires_deep_focus: true if sustained concentration needed.\n"
                        + "- suggested_duration_min: estimated focused minutes.\n"
                        + "- complexity_level: 0-5 complexity of task.\n"
                        + "- energy_demand: 0-5 mental/physical energy required.\n"
                        + "- relationship_value: 0-5 emotional/relationship/family value.\n"
                        + "Use conservative values when uncertain."));
        input.put(new JSONObject().put("role", "user").put("content",
                "Title: " + task.getTitle() + "\nDescription: " + (task.getDescription() == null ? "" : task.getDescription())));
        request.put("input", input);

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        JSONObject props = new JSONObject();
        props.put("risk_if_delayed", new JSONObject().put("type", "number"));
        props.put("requires_deep_focus", new JSONObject().put("type", "boolean"));
        props.put("suggested_duration_min", new JSONObject().put("type", "integer"));
        props.put("complexity_level", new JSONObject().put("type", "integer"));
        props.put("energy_demand", new JSONObject().put("type", "integer"));
        props.put("relationship_value", new JSONObject().put("type", "integer"));
        schema.put("properties", props);
        schema.put("required", new JSONArray()
                .put("risk_if_delayed")
                .put("requires_deep_focus")
                .put("suggested_duration_min")
                .put("complexity_level")
                .put("energy_demand")
                .put("relationship_value"));

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
        @Nullable public Integer suggestedDurationMin;
        public int complexityLevel;
        public int energyDemand;
        public int relationshipValue;
        public String descriptionHash;
        public String rawJson;
        public String model;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
