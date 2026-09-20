package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.SavedSearchItem;
import com.da_grupo9.ronda.data.model.SavedSearchRequest;
import com.da_grupo9.ronda.data.model.SavedSearchesResponse;
import com.da_grupo9.ronda.data.remote.SavedSearchesApi;
import com.da_grupo9.ronda.util.ApiError;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class SavedSearchesRepository {
    private final SavedSearchesApi api;

    @Inject public SavedSearchesRepository(SavedSearchesApi api) { this.api = api; }

    public void create(SavedSearchRequest request, RepositoryResult<SavedSearchItem> resultado) {
        ejecutar(api.createSavedSearch(request), "No se pudo guardar la búsqueda", false, resultado);
    }

    public void getAll(RepositoryResult<SavedSearchesResponse> resultado) {
        ejecutar(api.getSavedSearches(), "No se pudieron cargar las búsquedas", false, resultado);
    }

    public void markAsRead(String id, RepositoryResult<Void> resultado) {
        ejecutar(api.markAsRead(id), "No se pudo abrir la búsqueda", true, resultado);
    }

    public void delete(String id, RepositoryResult<Void> resultado) {
        ejecutar(api.deleteSavedSearch(id), "No se pudo eliminar la búsqueda", true, resultado);
    }

    private <T> void ejecutar(Call<T> call, String fallback, boolean permiteCuerpoVacio,
                              RepositoryResult<T> resultado) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && (permiteCuerpoVacio || response.body() != null)) {
                    resultado.onSuccess(response.body());
                } else {
                    resultado.onError(ApiError.from(response, fallback));
                }
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                resultado.onError(error instanceof IOException ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
    }
}
