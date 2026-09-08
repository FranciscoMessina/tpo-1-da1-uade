package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Contrato provisional: ajustar rutas y DTOs al integrar el backend definitivo. */
public interface PublicacionApi {
    @GET("publications") Call<PublicacionesResponse> getPublicaciones();
    @GET("publications/{id}") Call<Publicacion> getPublicacion(@Path("id") int id);
    @GET("me/publications") Call<List<Publicacion>> getPublicacionesPropias();
    @POST("publications/drafts") Call<Publicacion> crearBorrador(@Body Publicacion publicacion);
    @POST("publications/{id}/publish") Call<Publicacion> publicar(@Path("id") int id);
    @PATCH("publications/{id}/status")
    Call<Publicacion> cambiarEstado(@Path("id") int id, @Body Map<String, String> estado);
}
