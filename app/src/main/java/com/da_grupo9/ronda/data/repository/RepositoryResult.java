package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.util.ApiError;

/** Resultado común para las operaciones asíncronas de los repositorios. */
public interface RepositoryResult<T> {
    void onSuccess(T data);

    /** Permite advertir cuando el dato no fue confirmado por el servidor. */
    default void onSuccess(T data, boolean desdeCache) {
        onSuccess(data);
    }

    void onError(String mensaje);

    default void onError(ApiError error) {
        onError(error.getMessage());
    }
}
