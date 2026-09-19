package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.UploadImageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageUploadApi {
    @Multipart
    @POST("uploads/images")
    Call<UploadImageResponse> uploadImage(@Part MultipartBody.Part file);
}
