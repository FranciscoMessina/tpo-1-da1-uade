package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.CreateReviewRequest;
import com.da_grupo9.ronda.data.model.Operation;
import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.model.OperationsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProfileApi {
    @GET("me") Call<Perfil> getMe();
    @GET("me/operations")
    Call<OperationsResponse> getOperations(@Query("type") String type,
                                           @Query("from") String from,
                                           @Query("to") String to);
    @GET("operations/{id}")
    Call<Operation> getOperation(@Path("id") String operationId);
    @POST("operations/{id}/reviews")
    Call<Void> createReview(@Path("id") String operationId, @Body CreateReviewRequest request);
    @PATCH("me") Call<Perfil> updateMe(@Body Perfil perfil);
}
