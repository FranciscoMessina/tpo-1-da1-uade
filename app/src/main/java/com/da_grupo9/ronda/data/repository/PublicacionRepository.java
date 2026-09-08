package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.remote.PublicacionApi;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class PublicacionRepository {
    public interface Resultado<T> {
        void onSuccess(T data);
        void onError(String mensaje);
    }

    private final PublicacionApi api;

    @Inject
    public PublicacionRepository(PublicacionApi api) {
        this.api = api;
    }

    public void getPublicaciones(Resultado<List<Publicacion>> resultado) {
        api.getPublicaciones().enqueue(new Callback<PublicacionesResponse>() {
            @Override public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) resultado.onSuccess(response.body().getItems());
                else resultado.onError("El servidor respondió con código " + response.code());
            }
            @Override public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                resultado.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void getPublicacionById(int id, Resultado<Publicacion> resultado) {
        ejecutar(api.getPublicacion(id), resultado);
    }

    public void getPublicacionesPorVendedor(String email, Resultado<List<Publicacion>> resultado) {
        api.getPublicaciones().enqueue(new Callback<PublicacionesResponse>() {
            @Override public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resultado.onError("El servidor respondió con código " + response.code());
                    return;
                }
                List<Publicacion> filtradas = new ArrayList<>();
                for (Publicacion item : response.body().getItems()) {
                    if (item.getVendedorEmail() != null && item.getVendedorEmail().equalsIgnoreCase(email)) filtradas.add(item);
                }
                resultado.onSuccess(filtradas);
            }
            @Override public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                resultado.onError("No se pudo conectar con el servidor");
            }
        });
    }

    public void getMisPublicaciones(Resultado<List<Publicacion>> resultado) {
        ejecutar(api.getPublicacionesPropias(), resultado);
    }

    public void agregarPublicacion(Publicacion publicacion, Resultado<Publicacion> resultado) {
        api.crearBorrador(publicacion).enqueue(new Callback<Publicacion>() {
            @Override public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resultado.onError("No se pudo crear el borrador (código " + response.code() + ")");
                    return;
                }
                ejecutar(api.publicar(response.body().getId()), resultado);
            }
            @Override public void onFailure(Call<Publicacion> call, Throwable error) {
                resultado.onError("No se pudo conectar con el servidor");
            }
        });
    }

    public void cambiarEstadoPublicacion(int id, String estado, Resultado<Publicacion> resultado) {
        ejecutar(api.cambiarEstado(id, Collections.singletonMap("estadoPublicacion", estado)), resultado);
    }

    private <T> void ejecutar(Call<T> call, Resultado<T> resultado) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultado.onSuccess(response.body());
                } else {
                    resultado.onError("El servidor respondió con código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                resultado.onError(error instanceof IOException
                        ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
    }
}
