package com.da_grupo9.ronda.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.da_grupo9.ronda.data.local.PublicacionDao;
import com.da_grupo9.ronda.data.local.PublicacionEntity;
import com.da_grupo9.ronda.data.local.PublicacionMapper;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.remote.PublicacionApi;
import com.da_grupo9.ronda.util.ImageStorageManager;
import com.da_grupo9.ronda.util.NetworkMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final PublicacionDao publicacionDao;
    private final NetworkMonitor networkMonitor;
    private final ImageStorageManager imageStorageManager;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public PublicacionRepository(
            PublicacionApi api,
            PublicacionDao publicacionDao,
            NetworkMonitor networkMonitor,
            ImageStorageManager imageStorageManager) {
        this.api = api;
        this.publicacionDao = publicacionDao;
        this.networkMonitor = networkMonitor;
        this.imageStorageManager = imageStorageManager;
    }

    public boolean isOnline() {
        return networkMonitor.isOnline();
    }

    public NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

    public void getPublicaciones(Resultado<List<Publicacion>> resultado) {
        if (!networkMonitor.isOnline()) {
            obtenerPublicacionesDeCache(resultado, "Sin conexión a internet y no hay publicaciones guardadas");
            return;
        }

        api.getPublicaciones().enqueue(new Callback<PublicacionesResponse>() {
            @Override
            public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Publicacion> items = response.body().getItems();
                    guardarPublicacionesEnCache(items);
                    mainHandler.post(() -> resultado.onSuccess(items));
                } else {
                    obtenerPublicacionesDeCache(resultado, "El servidor respondió con código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                obtenerPublicacionesDeCache(resultado, error instanceof IOException
                        ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void getPublicacionById(String id, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            obtenerPublicacionDeCache(id, resultado, "Sin conexión a internet y esta publicación no está guardada");
            return;
        }

        api.getPublicacion(id).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Publicacion publicacion = response.body();
                    guardarConsultaDetalle(publicacion);
                    mainHandler.post(() -> resultado.onSuccess(publicacion));
                } else {
                    obtenerPublicacionDeCache(id, resultado, "El servidor respondió con código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable error) {
                obtenerPublicacionDeCache(id, resultado, error instanceof IOException
                        ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void getUltimasConsultadas(int limit, Resultado<List<Publicacion>> resultado) {
        dbExecutor.execute(() -> {
            List<PublicacionEntity> entities = publicacionDao.getUltimasConsultadas(limit);
            List<Publicacion> modelos = new ArrayList<>();
            for (PublicacionEntity entity : entities) {
                Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                if (pub != null) modelos.add(pub);
            }
            mainHandler.post(() -> resultado.onSuccess(modelos));
        });
    }

    private void guardarPublicacionesEnCache(List<Publicacion> items) {
        if (items == null || items.isEmpty()) return;
        dbExecutor.execute(() -> {
            List<PublicacionEntity> entities = new ArrayList<>();
            for (Publicacion pub : items) {
                PublicacionEntity entity = PublicacionMapper.toEntity(pub, imageStorageManager);
                if (entity != null) entities.add(entity);
            }
            publicacionDao.insertOrUpdateAll(entities);

            // Descargar imágenes de portada en segundo plano
            for (Publicacion pub : items) {
                if (pub.getCoverImage() != null) {
                    imageStorageManager.downloadAndSaveImageAsync(pub.getCoverImage(), null);
                }
            }
        });
    }

    private void guardarConsultaDetalle(Publicacion publicacion) {
        if (publicacion == null || publicacion.getId() == null) return;
        dbExecutor.execute(() -> {
            PublicacionEntity entity = PublicacionMapper.toEntity(publicacion, imageStorageManager);
            if (entity != null) {
                entity.setLastConsultedAt(System.currentTimeMillis());
                publicacionDao.insertOrUpdate(entity);
            }

            // Descargar todas las imágenes del detalle localmente
            if (publicacion.getCoverImage() != null) {
                imageStorageManager.downloadAndSaveImageAsync(publicacion.getCoverImage(), null);
            }
            List<String> fotos = publicacion.getImagenes();
            if (fotos != null) {
                for (String url : fotos) {
                    imageStorageManager.downloadAndSaveImageAsync(url, null);
                }
            }
        });
    }

    private void obtenerPublicacionesDeCache(Resultado<List<Publicacion>> resultado, String fallbackError) {
        dbExecutor.execute(() -> {
            List<PublicacionEntity> cached = publicacionDao.getPublicacionesHome();
            if (cached != null && !cached.isEmpty()) {
                List<Publicacion> modelos = new ArrayList<>();
                for (PublicacionEntity entity : cached) {
                    Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                    if (pub != null) modelos.add(pub);
                }
                mainHandler.post(() -> resultado.onSuccess(modelos));
            } else {
                mainHandler.post(() -> resultado.onError(fallbackError));
            }
        });
    }

    private void obtenerPublicacionDeCache(String id, Resultado<Publicacion> resultado, String fallbackError) {
        dbExecutor.execute(() -> {
            PublicacionEntity entity = publicacionDao.getById(id);
            if (entity != null) {
                // Registrar consulta
                publicacionDao.actualizarUltimaConsulta(id, System.currentTimeMillis());
                Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                mainHandler.post(() -> resultado.onSuccess(pub));
            } else {
                mainHandler.post(() -> resultado.onError(fallbackError));
            }
        });
    }

    public void getUsuario(String id, Resultado<PublicUser> resultado) {
        ejecutar(api.getUsuario(id), resultado);
    }

    public void getMisPublicaciones(Resultado<List<Publicacion>> resultado) {
        api.getPublicacionesPropias().enqueue(new Callback<PublicacionesResponse>() {
            @Override public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) resultado.onSuccess(response.body().getItems());
                else resultado.onError("El servidor respondió con código " + response.code());
            }
            @Override public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                resultado.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void agregarPublicacion(Publicacion publicacion, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para publicar un artículo");
            return;
        }
        api.crearBorrador().enqueue(new Callback<Publicacion>() {
            @Override public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    resultado.onError("No se pudo crear el borrador (código " + response.code() + ")");
                    return;
                }
                String id = response.body().getId();
                api.actualizarBorrador(id, publicacion).enqueue(new Callback<Publicacion>() {
                    @Override public void onResponse(Call<Publicacion> call, Response<Publicacion> updateResponse) {
                        if (updateResponse.isSuccessful()) {
                            publicar(id, updateResponse.body() != null ? updateResponse.body() : publicacion, resultado);
                        } else {
                            resultado.onError("No se pudo completar el borrador (código " + updateResponse.code() + ")");
                        }
                    }
                    @Override public void onFailure(Call<Publicacion> call, Throwable error) {
                        resultado.onError("No se pudo conectar con el servidor");
                    }
                });
            }
            @Override public void onFailure(Call<Publicacion> call, Throwable error) {
                resultado.onError("No se pudo conectar con el servidor");
            }
        });
    }

    public void cambiarEstadoPublicacion(String id, String estado, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para modificar el estado");
            return;
        }
        ejecutar(api.cambiarEstado(id, Collections.singletonMap("status", estado)), resultado);
    }

    public void actualizarPublicacion(String id, Publicacion publicacion, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para actualizar la publicación");
            return;
        }
        ejecutar(api.actualizarBorrador(id, publicacion), resultado);
    }

    private void publicar(String id, Publicacion publicacion, Resultado<Publicacion> resultado) {
        api.publicar(id).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) resultado.onSuccess(publicacion);
                else resultado.onError("No se pudo publicar (código " + response.code() + ")");
            }

            @Override public void onFailure(Call<Void> call, Throwable error) {
                resultado.onError(error instanceof IOException
                        ? "No se pudo conectar con el servidor"
                        : "No se pudo procesar la respuesta del servidor");
            }
        });
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
