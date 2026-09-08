package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.Perfil;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface ProfileApi {
    @GET("me") Call<Perfil> getMe();
    @PATCH("me") Call<Perfil> updateMe(@Body Perfil perfil);
}
