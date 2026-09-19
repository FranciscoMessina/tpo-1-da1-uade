package com.da_grupo9.ronda.data.local;

import android.text.TextUtils;

import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicationImage;
import com.da_grupo9.ronda.util.ImageStorageManager;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PublicacionMapper {

    private static final Gson gson = new Gson();

    public static PublicacionEntity toEntity(String accountId, Publicacion publicacion, ImageStorageManager imageStorageManager) {
        if (publicacion == null) return null;

        PublicacionEntity entity = new PublicacionEntity();
        entity.setAccountId(accountId);
        entity.setId(publicacion.getId() != null ? publicacion.getId() : "");
        entity.setTitle(publicacion.getTitulo());
        entity.setDescription(publicacion.getDescripcion());
        entity.setCategory(publicacion.getCategoryApiValue());
        entity.setPrice(publicacion.getPrecio());
        entity.setItemCondition(publicacion.getConditionApiValue());
        entity.setZone(publicacion.getZona());
        entity.setStatus(publicacion.getEstadoPublicacion());
        entity.setPublishedAt(publicacion.getFechaPublicacion());
        entity.setSellerName(publicacion.getVendedorNombre());
        entity.setSellerRating(publicacion.getVendedorReputacion());
        entity.setCoverImage(publicacion.getCoverImage());
        if (imageStorageManager != null && !TextUtils.isEmpty(publicacion.getCoverImage())) {
            entity.setLocalCoverImagePath(imageStorageManager.getLocalImagePathIfExists(publicacion.getCoverImage()));
        }
        entity.setFullJson(toJson(publicacion));
        entity.setCachedAt(System.currentTimeMillis());
        return entity;
    }

    public static String toJson(Publicacion publicacion) {
        return gson.toJson(publicacion);
    }

    public static Publicacion toModel(PublicacionEntity entity, ImageStorageManager imageStorageManager) {
        if (entity == null) return null;

        Publicacion publicacion = null;
        if (!TextUtils.isEmpty(entity.getFullJson())) {
            try {
                publicacion = gson.fromJson(entity.getFullJson(), Publicacion.class);
            } catch (Exception ignored) {
            }
        }

        if (publicacion == null) {
            publicacion = new Publicacion(
                    entity.getTitle(),
                    entity.getDescription(),
                    entity.getPrice(),
                    entity.getItemCondition(),
                    entity.getCategory(),
                    entity.getZone(),
                    0
            );
        }

        if (imageStorageManager != null) {
            List<String> originalUrls = publicacion.getImagenes();
            if (!originalUrls.isEmpty()) {
                List<PublicationImage> localImages = new ArrayList<>();
                for (int i = 0; i < originalUrls.size(); i++) {
                    String url = originalUrls.get(i);
                    String localPath = imageStorageManager.getLocalImagePathIfExists(url);
                    String pathToUse = (localPath != null && new File(localPath).exists()) ? localPath : url;
                    localImages.add(new PublicationImage(String.valueOf(i), pathToUse, i));
                }
                publicacion.setImages(localImages);
            }
            if (publicacion.getCoverImage() != null) {
                String localCover = imageStorageManager.getLocalImagePathIfExists(publicacion.getCoverImage());
                if (localCover != null && new File(localCover).exists()) {
                    publicacion.setCoverImage(localCover);
                }
            }
        }

        return publicacion;
    }
}
