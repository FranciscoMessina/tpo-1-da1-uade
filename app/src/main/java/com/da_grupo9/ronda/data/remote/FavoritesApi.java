package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.FavoriteResponse;
import com.da_grupo9.ronda.data.model.FavoritesResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesApi {

    @POST("publications/{id}/favorite")
    Call<FavoriteResponse> addFavorite(@Path("id") String publicationId);

    @DELETE("publications/{id}/favorite")
    Call<FavoriteResponse> removeFavorite(@Path("id") String publicationId);

    @GET("me/favorites")
    Call<FavoritesResponse> getFavorites();
}