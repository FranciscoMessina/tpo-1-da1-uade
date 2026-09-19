package com.da_grupo9.ronda.util;

import retrofit2.Response;

/** Obtiene el mensaje legible enviado por la API en una respuesta HTTP de error. */
public final class ApiErrorMessage {
    private ApiErrorMessage() {}

    public static String from(Response<?> response, String fallback) {
        return ApiError.from(response, fallback).getMessage();
    }
}
