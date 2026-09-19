package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.model.UploadImageResponse;
import com.da_grupo9.ronda.data.model.OperationsResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ProfileApi {
    @GET("me") Call<Perfil> getMe();
    @GET("me/operations") Call<OperationsResponse> getOperations();
    @PATCH("me") Call<Perfil> updateMe(@Body Perfil perfil);
    @Multipart
    @retrofit2.http.POST("uploads/images")
    Call<UploadImageResponse> uploadImage(@Part MultipartBody.Part file);
}
