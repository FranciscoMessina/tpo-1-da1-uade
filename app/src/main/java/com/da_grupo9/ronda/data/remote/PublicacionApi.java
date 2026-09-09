package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.model.PublicUser;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PublicacionApi {
    @GET("publications") Call<PublicacionesResponse> getPublicaciones();
    @GET("users/{id}") Call<PublicUser> getUsuario(@Path("id") String id);
    @GET("publications/{id}") Call<Publicacion> getPublicacion(@Path("id") String id);
    @GET("me/publications") Call<PublicacionesResponse> getPublicacionesPropias();
    @POST("publications/drafts") Call<Publicacion> crearBorrador();
    @PATCH("publications/{id}") Call<Publicacion> actualizarBorrador(@Path("id") String id, @Body Publicacion publicacion);
    @POST("publications/{id}/publish") Call<Void> publicar(@Path("id") String id);
    @PATCH("publications/{id}/status")
    Call<Publicacion> cambiarEstado(@Path("id") String id, @Body Map<String, String> estado);
}
