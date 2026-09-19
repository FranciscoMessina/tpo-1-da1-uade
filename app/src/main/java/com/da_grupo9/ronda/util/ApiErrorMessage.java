package com.da_grupo9.ronda.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/** Obtiene el mensaje legible enviado por la API en una respuesta HTTP de error. */
public final class ApiErrorMessage {
    private static final String[] MESSAGE_KEYS = {"message", "mensaje", "detail", "error"};

    private ApiErrorMessage() {}

    public static String from(Response<?> response, String fallback) {
        if (response == null) return fallback;

        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) return fallback;

        try {
            String raw = errorBody.string().trim();
            if (raw.isEmpty()) return fallback;

            try {
                String message = findMessage(JsonParser.parseString(raw));
                return isBlank(message) ? fallback : message.trim();
            } catch (RuntimeException ignored) {
                // Algunas APIs responden el error directamente como texto plano.
                return raw.startsWith("<") ? fallback : raw;
            }
        } catch (IOException ignored) {
            return fallback;
        }
    }

    private static String findMessage(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        if (!element.isJsonObject()) return null;

        JsonObject object = element.getAsJsonObject();
        for (String key : MESSAGE_KEYS) {
            if (!object.has(key)) continue;
            String message = findMessage(object.get(key));
            if (!isBlank(message)) return message;
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
