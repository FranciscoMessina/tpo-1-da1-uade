package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.FavoriteResponse;
import com.da_grupo9.ronda.data.model.FavoritesReadResponse;
import com.da_grupo9.ronda.data.model.FavoritesResponse;
import com.da_grupo9.ronda.data.remote.FavoritesApi;
import com.da_grupo9.ronda.util.ApiError;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class FavoritesRepository {
    private final FavoritesApi api;

    @Inject public FavoritesRepository(FavoritesApi api) { this.api = api; }

    public void getFavorites(RepositoryResult<FavoritesResponse> resultado) {
        ejecutar(api.getFavorites(), "No se pudieron cargar los favoritos", resultado);
    }

    public void markAsRead(RepositoryResult<FavoritesReadResponse> resultado) {
        ejecutar(api.markFavoritesAsRead(), "No se pudieron marcar los favoritos como leídos", resultado);
    }

    public void setFavorite(String publicationId, boolean favorite,
                            RepositoryResult<FavoriteResponse> resultado) {
        if (favorite) {
            ejecutar(api.addFavorite(publicationId), "No se pudo guardar la publicación", resultado);
        } else {
            ejecutar(api.removeFavorite(publicationId), "No se pudo quitar de favoritos", resultado);
        }
    }

    private <T> void ejecutar(Call<T> call, String fallback, RepositoryResult<T> resultado) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) resultado.onSuccess(response.body());
                else resultado.onError(ApiError.from(response, fallback));
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                resultado.onError(error instanceof IOException ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
    }
}
