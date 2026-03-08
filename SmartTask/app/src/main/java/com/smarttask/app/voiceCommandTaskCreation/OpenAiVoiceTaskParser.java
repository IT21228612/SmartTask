package com.smarttask.app.voiceCommandTaskCreation;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OpenAiVoiceTaskParser {

    public interface Callback {
        void onParsed(ParsedVoiceTask parsedVoiceTask, boolean usedFallback);
    }

    private static final String RESPONSES_API_URL = "https://api.openai.com/v1/responses";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient();

    public ParsedVoiceTask parseOrFallback(Context context, String transcript) {
        String exactTranscript = transcript == null ? "" : transcript;
        String apiKey = OpenAiManifestKeyReader.getOpenAiApiKey(context);
        if (TextUtils.isEmpty(apiKey)) {
            return createFallback(exactTranscript);
        }

        try {
            Request request = new Request.Builder()
                    .url(RESPONSES_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(buildRequestJson(exactTranscript).toString(), JSON))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return createFallback(exactTranscript);
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    return createFallback(exactTranscript);
                }
                String body = responseBody.string();
                ParsedVoiceTask parsed = parseResponseBody(body, exactTranscript);
                return parsed == null ? createFallback(exactTranscript) : parsed;
            }
        } catch (IOException | JSONException e) {
            return createFallback(exactTranscript);
        }
    }

    private JSONObject buildRequestJson(String transcript) throws JSONException {
        ZonedDateTime localNow = ZonedDateTime.now();
        JSONObject request = new JSONObject();
        request.put("model", "gpt-4.1-mini");

        JSONArray input = new JSONArray();
        input.put(new JSONObject()
                .put("role", "system")
                .put("content", buildSystemInstruction()));
        input.put(new JSONObject()
                .put("role", "user")
                .put("content", buildUserPrompt(transcript, localNow)));

        request.put("input", input);

        JSONObject textFormat = new JSONObject()
                .put("type", "json_schema")
                .put("name", "voice_task_fields")
                .put("strict", true)
                .put("schema", buildStrictSchema());

        request.put("text", new JSONObject().put("format", textFormat));
        return request;
    }

    private JSONObject buildStrictSchema() throws JSONException {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("additionalProperties", false);

        JSONObject properties = new JSONObject();
        properties.put("task_title", new JSONObject().put("type", "string"));
        properties.put("description", new JSONObject().put("type", "string"));
        properties.put("category", new JSONObject().put("type", "string"));
        properties.put("priority", new JSONObject().put("type", "string"));
        properties.put("due_datetime", new JSONObject().put("type", "string"));
        properties.put("preferred_start_datetime", new JSONObject().put("type", "string"));
        properties.put("preferred_end_datetime", new JSONObject().put("type", "string"));
        properties.put("location_radius_meters", new JSONObject().put("type", "string"));
        properties.put("enable_notifications", new JSONObject().put("type", "string"));
        properties.put("raw_transcript", new JSONObject().put("type", "string"));

        schema.put("properties", properties);

        JSONArray required = new JSONArray();
        required.put("task_title");
        required.put("description");
        required.put("category");
        required.put("priority");
        required.put("due_datetime");
        required.put("preferred_start_datetime");
        required.put("preferred_end_datetime");
        required.put("location_radius_meters");
        required.put("enable_notifications");
        required.put("raw_transcript");
        schema.put("required", required);
        return schema;
    }

    private String buildSystemInstruction() {
        return "Extract task creation fields from a spoken command. "
                + "Return ONLY valid JSON fields for this schema and nothing else. "
                + "Resolve relative dates/times using the provided current local date/time reference and timezone. "
                + "Do not guess dates/times when that reference is missing or unclear. "
                + "If a value is not clearly specified, return an empty string. "
                + "task_title should be blank if uncertain. "
                + "description should include extra details if present. "
                + "category must be exactly one of Work, Study, Home, Personal; otherwise blank. "
                + "priority must be exactly one of Low, Medium, High; otherwise blank. "
                + "due_datetime, preferred_start_datetime, preferred_end_datetime should be ISO-8601 only when clearly mentioned. "
                + "Do not return location coordinates or location label. "
                + "location_radius_meters only when clearly mentioned, numeric string otherwise blank. "
                + "enable_notifications should be true/false string only when clearly implied or explicitly requested; otherwise blank. "
                + "Preserve the full transcript exactly in raw_transcript. "
                + "Never invent missing information.";
    }

    private String buildUserPrompt(String transcript, ZonedDateTime localNow) {
        return "Current local date: " + localNow.toLocalDate() + "\n"
                + "Current local time: " + localNow.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n"
                + "Timezone: " + TimeZone.getDefault().getID() + "\n"
                + "Transcript: " + transcript;
    }

    @Nullable
    private ParsedVoiceTask parseResponseBody(String body, String transcript) throws JSONException {
        JSONObject root = new JSONObject(body);
        String jsonText = extractStructuredJson(root);
        if (TextUtils.isEmpty(jsonText)) {
            return null;
        }

        JSONObject result = new JSONObject(jsonText);
        ParsedVoiceTask parsedVoiceTask = new ParsedVoiceTask(transcript);
        parsedVoiceTask.setExtractedJson(jsonText);
        parsedVoiceTask.setTaskTitle(blankToNull(result.optString("task_title", "")));
        parsedVoiceTask.setDescription(blankToNull(result.optString("description", "")));
        parsedVoiceTask.setCategory(validateCategory(result.optString("category", "")));
        parsedVoiceTask.setPriority(validatePriority(result.optString("priority", "")));
        parsedVoiceTask.setDueDatetime(blankToNull(result.optString("due_datetime", "")));
        parsedVoiceTask.setPreferredStartDatetime(blankToNull(result.optString("preferred_start_datetime", "")));
        parsedVoiceTask.setPreferredEndDatetime(blankToNull(result.optString("preferred_end_datetime", "")));
        parsedVoiceTask.setLocationRadiusMeters(parsePositiveInt(result.optString("location_radius_meters", "")));
        parsedVoiceTask.setEnableNotifications(parseNullableBoolean(result.optString("enable_notifications", "")));

        String rawTranscript = result.optString("raw_transcript", "");
        parsedVoiceTask.setRawTranscript(TextUtils.isEmpty(rawTranscript) ? transcript : rawTranscript);
        return parsedVoiceTask;
    }

    @Nullable
    private String extractStructuredJson(JSONObject root) {
        String outputText = root.optString("output_text", null);
        if (!TextUtils.isEmpty(outputText)) {
            return outputText;
        }

        JSONArray output = root.optJSONArray("output");
        if (output == null || output.length() == 0) {
            return null;
        }

        JSONObject item = output.optJSONObject(0);
        if (item == null) {
            return null;
        }

        JSONArray content = item.optJSONArray("content");
        if (content == null || content.length() == 0) {
            return null;
        }

        JSONObject contentItem = content.optJSONObject(0);
        if (contentItem == null) {
            return null;
        }
        return contentItem.optString("text", null);
    }

    @Nullable
    private Integer parsePositiveInt(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private Boolean parseNullableBoolean(String value) {
        String trimmed = value == null ? "" : value.trim().toLowerCase(Locale.US);
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("true".equals(trimmed)) {
            return true;
        }
        if ("false".equals(trimmed)) {
            return false;
        }
        return null;
    }

    @Nullable
    private String validateCategory(String value) {
        String normalized = normalize(value);
        if ("Work".equals(normalized) || "Study".equals(normalized)
                || "Home".equals(normalized) || "Personal".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    @Nullable
    private String validatePriority(String value) {
        String normalized = normalize(value);
        if ("Low".equals(normalized) || "Medium".equals(normalized) || "High".equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String normalize(String value) {
        String trimmed = value == null ? "" : value.trim().toLowerCase(Locale.US);
        if (trimmed.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    @Nullable
    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    @NonNull
    public ParsedVoiceTask createFallback(String transcript) {
        ParsedVoiceTask fallback = new ParsedVoiceTask(transcript);
        fallback.setTaskTitle("Voice task");
        fallback.setDescription(transcript);
        return fallback;
    }
}
