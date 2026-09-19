package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.SavedSearchItem;
import com.da_grupo9.ronda.data.model.SavedSearchRequest;
import com.da_grupo9.ronda.data.model.SavedSearchesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SavedSearchesApi {

    @POST("saved-searches")
    Call<SavedSearchItem> createSavedSearch(
            @Body SavedSearchRequest request
    );

    @GET("saved-searches")
    Call<SavedSearchesResponse> getSavedSearches();

    @POST("saved-searches/{id}/read")
    Call<Void> markAsRead(
            @Path("id") String savedSearchId
    );

    @DELETE("saved-searches/{id}")
    Call<Void> deleteSavedSearch(
            @Path("id") String savedSearchId
    );
}