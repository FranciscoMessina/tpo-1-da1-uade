package com.da_grupo9.ronda.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.da_grupo9.ronda.data.local.PublicacionDao;
import com.da_grupo9.ronda.data.local.PublicacionEntity;
import com.da_grupo9.ronda.data.local.PublicacionMapper;
import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.FiltrosPublicaciones;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.model.PublicationRequest;
import com.da_grupo9.ronda.data.model.ReviewsResponse;
import com.da_grupo9.ronda.data.model.CategoriesResponse;
import com.da_grupo9.ronda.data.model.ZonesResponse;
import com.da_grupo9.ronda.data.model.QuestionRequest;
import com.da_grupo9.ronda.data.model.AnswerQuestionRequest;
import com.da_grupo9.ronda.data.remote.PublicacionApi;
import com.da_grupo9.ronda.util.ImageStorageManager;
import com.da_grupo9.ronda.util.ImageUploadManager;
import com.da_grupo9.ronda.util.NetworkMonitor;
import com.da_grupo9.ronda.util.ApiError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Comparator;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class PublicacionRepository {

    public interface Resultado<T> {
        void onSuccess(T data);
        /** Permite a las pantallas advertir cuando el dato no fue confirmado por el servidor. */
        default void onSuccess(T data, boolean desdeCache) { onSuccess(data); }
        void onError(String mensaje);
        default void onError(ApiError error) { onError(error.getMessage()); }
    }

    public interface ResultadoPagina {
        void onSuccess(List<Publicacion> items, int page, int totalPages, int total);
        /** Permite a Home advertir cuando la página procede de la caché local. */
        default void onSuccess(List<Publicacion> items, int page, int totalPages, int total,
                               boolean desdeCache) {
            onSuccess(items, page, totalPages, total);
        }
        void onError(String mensaje);
        default void onError(ApiError error) { onError(error.getMessage()); }
    }

    private final PublicacionApi api;
    private final PublicacionDao publicacionDao;
    private final NetworkMonitor networkMonitor;
    private final ImageStorageManager imageStorageManager;
    private final ImageUploadManager imageUploadManager;
    private final SessionManager sessionManager;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public PublicacionRepository(
            PublicacionApi api,
            PublicacionDao publicacionDao,
            NetworkMonitor networkMonitor,
            ImageStorageManager imageStorageManager,
            ImageUploadManager imageUploadManager,
            SessionManager sessionManager) {
        this.api = api;
        this.publicacionDao = publicacionDao;
        this.networkMonitor = networkMonitor;
        this.imageStorageManager = imageStorageManager;
        this.imageUploadManager = imageUploadManager;
        this.sessionManager = sessionManager;
    }

    /** La caché se separa por cuenta; sin sesión se usa el espacio anónimo. */
    private String cuentaActual() {
        String userId = sessionManager.getUserId();
        return userId != null && sessionManager.isLoggedIn() ? userId : "";
    }

    public boolean isOnline() {
        return networkMonitor.isOnline();
    }

    public NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

    public void getPublicaciones(int page, int pageSize, FiltrosPublicaciones filtros,
                                 ResultadoPagina resultado) {
        String query = filtros.getQuery();
        String category = filtros.getCategory();
        String condition = filtros.getCondition();
        String zone = filtros.getZone();
        Double minPrice = filtros.getMinPrice();
        Double maxPrice = filtros.getMaxPrice();
        String sort = filtros.getSort();
        if (!networkMonitor.isOnline()) {
            obtenerPublicacionesDeCache(page, pageSize, query, category, condition, zone,
                    minPrice, maxPrice, sort, resultado,
                    "Sin conexión a internet y no hay publicaciones guardadas");
            return;
        }

        // Las escrituras pendientes se guardan bajo la cuenta que hizo la petición,
        // aunque la sesión cambie antes de que llegue la respuesta.
        String cuenta = cuentaActual();
        api.getPublicaciones(page, pageSize, query, category, condition, zone, minPrice, maxPrice, sort)
                .enqueue(new Callback<PublicacionesResponse>() {
            @Override
            public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Publicacion> items = response.body().getItems();
                    guardarPublicacionesEnCache(cuenta, items);
                    PublicacionesResponse.Pagination pagination = response.body().getPagination();
                    int responsePage = pagination != null ? pagination.getPage() : page;
                    int totalPages = pagination != null ? pagination.getTotalPages() : 1;
                    int total = pagination != null ? pagination.getTotal() : items.size();
                    mainHandler.post(() -> resultado.onSuccess(
                            items, responsePage, totalPages, total, false));
                } else {
                    resultado.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                obtenerPublicacionesDeCache(page, pageSize, query, category, condition, zone,
                        minPrice, maxPrice, sort, resultado, error instanceof IOException
                                ? "No se pudo conectar con el servidor"
                                : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void getCategories(Resultado<List<String>> resultado) {
        ejecutar(api.getCategories(), new Resultado<CategoriesResponse>() {
            @Override public void onSuccess(CategoriesResponse data) { resultado.onSuccess(data.getItems()); }
            @Override public void onError(String mensaje) { resultado.onError(mensaje); }
        });
    }

    public void getZones(Resultado<List<String>> resultado) {
        ejecutar(api.getZones(), new Resultado<ZonesResponse>() {
            @Override public void onSuccess(ZonesResponse data) { resultado.onSuccess(data.getItems()); }
            @Override public void onError(String mensaje) { resultado.onError(mensaje); }
        });
    }

    public void getPublicacionById(String id, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            obtenerPublicacionDeCache(id, resultado, "Sin conexión a internet y esta publicación no está guardada");
            return;
        }

        String cuenta = cuentaActual();
        api.getPublicacion(id).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Publicacion publicacion = response.body();
                    guardarConsultaDetalle(cuenta, publicacion);
                    mainHandler.post(() -> resultado.onSuccess(publicacion, false));
                } else {
                    // Un rechazo vigente (sesión, permisos o publicación inexistente) no debe
                    // quedar oculto detrás de una copia local anterior.
                    resultado.onError(ApiError.from(response,
                            "El servidor respondió con código " + response.code()));
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
        String cuenta = cuentaActual();
        dbExecutor.execute(() -> {
            List<PublicacionEntity> entities = publicacionDao.getUltimasConsultadas(cuenta, limit);
            List<Publicacion> modelos = new ArrayList<>();
            for (PublicacionEntity entity : entities) {
                Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                if (pub != null) modelos.add(pub);
            }
            mainHandler.post(() -> resultado.onSuccess(modelos));
        });
    }

    private void guardarPublicacionesEnCache(String cuenta, List<Publicacion> items) {
        if (items == null || items.isEmpty()) return;
        dbExecutor.execute(() -> {
            List<PublicacionEntity> entities = new ArrayList<>();
            for (Publicacion pub : items) {
                PublicacionEntity entity = PublicacionMapper.toEntity(cuenta, pub, imageStorageManager);
                if (entity == null) continue;
                conservarDetalle(entity, pub);
                entities.add(entity);
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

    /**
     * El resumen del feed no debe reemplazar un detalle ya consultado: se actualizan
     * sólo los campos compartidos y se conservan galería, permisos y marca de consulta.
     * Debe ejecutarse en dbExecutor.
     */
    private void conservarDetalle(PublicacionEntity nueva, Publicacion resumen) {
        PublicacionEntity existente = publicacionDao.getById(nueva.getAccountId(), nueva.getId());
        if (existente == null || !existente.isHasDetail()) return;
        Publicacion detalle = PublicacionMapper.toModel(existente, null);
        if (detalle == null) return;
        detalle.actualizarDesdeResumen(resumen);
        nueva.setFullJson(PublicacionMapper.toJson(detalle));
        nueva.setHasDetail(true);
        nueva.setLastConsultedAt(existente.getLastConsultedAt());
    }

    private void guardarConsultaDetalle(String cuenta, Publicacion publicacion) {
        if (publicacion == null || publicacion.getId() == null) return;
        dbExecutor.execute(() -> {
            PublicacionEntity entity = PublicacionMapper.toEntity(cuenta, publicacion, imageStorageManager);
            if (entity != null) {
                entity.setHasDetail(true);
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

    private void obtenerPublicacionesDeCache(int page, int pageSize, String query, String category,
                                             String condition, String zone, Double minPrice,
                                             Double maxPrice, String sort, ResultadoPagina resultado,
                                             String fallbackError) {
        String cuenta = cuentaActual();
        dbExecutor.execute(() -> {
            List<PublicacionEntity> cached = publicacionDao.getPublicacionesHome(cuenta);
            if (cached != null && !cached.isEmpty()) {
                List<Publicacion> modelos = new ArrayList<>();
                for (PublicacionEntity entity : cached) {
                    Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                    if (pub != null && coincide(pub, query, category, condition, zone, minPrice, maxPrice)) {
                        modelos.add(pub);
                    }
                }
                if ("price_asc".equals(sort)) modelos.sort(Comparator.comparingDouble(Publicacion::getPrecio));
                else if ("price_desc".equals(sort)) modelos.sort(Comparator.comparingDouble(Publicacion::getPrecio).reversed());
                else modelos.sort(Comparator.comparingInt(Publicacion::getFecha).reversed());

                int total = modelos.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
                int safePage = Math.min(Math.max(1, page), totalPages);
                int from = Math.min((safePage - 1) * pageSize, total);
                int to = Math.min(from + pageSize, total);
                List<Publicacion> pagina = new ArrayList<>(modelos.subList(from, to));
                mainHandler.post(() -> resultado.onSuccess(
                        pagina, safePage, totalPages, total, true));
            } else {
                mainHandler.post(() -> resultado.onError(fallbackError));
            }
        });
    }

    private boolean coincide(Publicacion publicacion, String query, String category, String condition,
                              String zone, Double minPrice, Double maxPrice) {
        String normalizedQuery = query == null ? null : query.toLowerCase(Locale.ROOT);
        boolean textMatches = normalizedQuery == null
                || (publicacion.getTitulo() != null && publicacion.getTitulo().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                || (publicacion.getDescripcion() != null && publicacion.getDescripcion().toLowerCase(Locale.ROOT).contains(normalizedQuery));
        return textMatches
                && (category == null || category.equals(publicacion.getCategoryApiValue()))
                && (condition == null || condition.equals(publicacion.getConditionApiValue()))
                && (zone == null || zone.equalsIgnoreCase(publicacion.getZona()))
                && (minPrice == null || publicacion.getPrecio() >= minPrice)
                && (maxPrice == null || publicacion.getPrecio() <= maxPrice);
    }

    private void obtenerPublicacionDeCache(String id, Resultado<Publicacion> resultado, String fallbackError) {
        String cuenta = cuentaActual();
        dbExecutor.execute(() -> {
            PublicacionEntity entity = publicacionDao.getById(cuenta, id);
            if (entity != null) {
                // Registrar consulta
                publicacionDao.actualizarUltimaConsulta(cuenta, id, System.currentTimeMillis());
                Publicacion pub = PublicacionMapper.toModel(entity, imageStorageManager);
                if (pub != null) {
                    mainHandler.post(() -> resultado.onSuccess(pub, true));
                } else {
                    mainHandler.post(() -> resultado.onError(fallbackError));
                }
            } else {
                mainHandler.post(() -> resultado.onError(fallbackError));
            }
        });
    }

    public void getUsuario(String id, Resultado<PublicUser> resultado) {
        ejecutar(api.getUsuario(id), resultado);
    }

    public void getResenasUsuario(String id, int page, int pageSize, Resultado<ReviewsResponse> resultado) {
        ejecutar(api.getResenasUsuario(id, page, pageSize), resultado);
    }

    public void getMisPublicaciones(Resultado<List<Publicacion>> resultado) {
        api.getPublicacionesPropias().enqueue(new Callback<PublicacionesResponse>() {
            @Override public void onResponse(Call<PublicacionesResponse> call, Response<PublicacionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) resultado.onSuccess(response.body().getItems());
                else resultado.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
            }
            @Override public void onFailure(Call<PublicacionesResponse> call, Throwable error) {
                resultado.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "No se pudo procesar la respuesta del servidor");
            }
        });
    }

    public void agregarPublicacion(PublicationRequest publicacion, List<String> rutasImagenes,
                                   Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para publicar un artículo");
            return;
        }
        imageUploadManager.uploadFiles(rutasImagenes, new ImageUploadManager.Result<List<String>>() {
            @Override public void onSuccess(List<String> urls) {
                PublicationRequest request = new PublicationRequest(
                        publicacion.getTitle(), publicacion.getDescription(),
                        publicacion.getCategory(), publicacion.getPrice(),
                        publicacion.getCondition(), publicacion.getAddress(), urls);
                ejecutar(api.crearPublicacion(request), resultado);
            }
            @Override public void onError(String mensaje) { resultado.onError(mensaje); }
        });
    }

    public void cambiarEstadoPublicacion(String id, String estado, Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para modificar el estado");
            return;
        }
        ejecutar(api.cambiarEstado(id, Collections.singletonMap("status", estado)), resultado);
    }

    public void actualizarPublicacion(String id, PublicationRequest publicacion,
                                      List<String> rutasImagenesNuevas,
                                      List<String> imagenesConservadas,
                                      Resultado<Publicacion> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para actualizar la publicación");
            return;
        }
        List<String> rutas = new ArrayList<>(rutasImagenesNuevas);
        List<String> conservadas = new ArrayList<>(imagenesConservadas);
        imageUploadManager.uploadFiles(rutas, new ImageUploadManager.Result<List<String>>() {
            @Override public void onSuccess(List<String> urlsNuevas) {
                List<String> urlsFinales = new ArrayList<>(conservadas);
                urlsFinales.addAll(urlsNuevas);
                PublicationRequest request = new PublicationRequest(
                        publicacion.getTitle(), publicacion.getDescription(),
                        publicacion.getCategory(), publicacion.getPrice(),
                        publicacion.getCondition(), publicacion.getAddress(), urlsFinales);
                ejecutar(api.actualizarPublicacion(id, request), resultado);
            }

            @Override public void onError(String mensaje) {
                resultado.onError(mensaje);
            }
        });
    }

    public void crearPregunta(String publicacionId, String texto,
                              Resultado<Publicacion.Question> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para enviar una pregunta");
            return;
        }
        ejecutar(api.crearPregunta(publicacionId, new QuestionRequest(texto)), resultado);
    }

    public void responderPregunta(String preguntaId, String respuesta,
                                  Resultado<Publicacion.Question> resultado) {
        if (!networkMonitor.isOnline()) {
            resultado.onError("Se necesita conexión a internet para responder la pregunta");
            return;
        }
        ejecutar(api.responderPregunta(preguntaId, new AnswerQuestionRequest(respuesta)), resultado);
    }

    private <T> void ejecutar(Call<T> call, Resultado<T> resultado) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultado.onSuccess(response.body());
                } else {
                    resultado.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
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
