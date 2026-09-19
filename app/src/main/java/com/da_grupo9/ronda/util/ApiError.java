package com.da_grupo9.ronda.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

/** Error estable de la API: el código sirve para decidir y el mensaje para presentar. */
public final class ApiError {
    private final String code;
    private final String message;

    public ApiError(String code, String message) {
        this.code = blankToNull(code);
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }

    public static ApiError from(Response<?> response, String fallback) {
        if (response == null || response.errorBody() == null) return new ApiError(null, fallback);
        try {
            return parse(response.errorBody().string(), fallback);
        } catch (IOException | RuntimeException ignored) {
            return new ApiError(null, fallback);
        }
    }

    /** Visible para pruebas y para respuestas ya materializadas. */
    public static ApiError parse(String raw, String fallback) {
        if (raw == null || raw.trim().isEmpty()) return new ApiError(null, fallback);
        try {
            JsonElement root = JsonParser.parseString(raw);
            if (!root.isJsonObject()) return new ApiError(null, fallback);
            JsonElement errorElement = root.getAsJsonObject().get("error");
            if (errorElement == null || !errorElement.isJsonObject()) return new ApiError(null, fallback);
            JsonObject error = errorElement.getAsJsonObject();
            String code = string(error, "code");
            String message = string(error, "message");
            return new ApiError(code, blankToNull(message) == null ? fallback : message.trim());
        } catch (RuntimeException ignored) {
            return new ApiError(null, fallback);
        }
    }

    public static ApiError local(String message) { return new ApiError(null, message); }

    public boolean isSessionInvalid() {
        String value = normalizedCode();
        return value.equals("UNAUTHORIZED") || value.equals("INVALID_SESSION")
                || value.equals("SESSION_EXPIRED") || value.equals("AUTHENTICATION_REQUIRED");
    }

    public boolean isOfferNoLongerActionable() {
        String value = normalizedCode();
        return value.contains("OFFER") && (value.contains("RESOLVED")
                || value.contains("EXPIRED") || value.contains("NOT_PENDING")
                || value.contains("INVALID_STATE"));
    }

    public boolean isReviewAlreadySubmitted() {
        String value = normalizedCode();
        return value.contains("REVIEW") && (value.contains("ALREADY") || value.contains("DUPLICATE"));
    }

    private String normalizedCode() {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString() : null;
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
