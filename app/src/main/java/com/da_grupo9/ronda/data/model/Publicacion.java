package com.da_grupo9.ronda.data.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** DTO compartido por el listado, el detalle y las publicaciones propias. */
public class Publicacion implements Serializable {
    private String id;
    private String sellerId;
    private String title;
    private String description;
    private String category;
    private Integer priceCents;
    private Double price;
    private String itemCondition;
    private String zone;
    private String status;
    private int draftStep;
    private String publishedAt;
    private String createdAt;
    private String updatedAt;
    private String sellerName;
    private String coverImage;
    private List<PublicationImage> images;
    private PublicUser seller;
    private List<Question> questions;
    private Boolean isFavorite;
    private Actions actions;

    public Publicacion(String title, String description, int priceCents,
                       String itemCondition, String category, String zone, int draftStep) {
        this.title = title;
        this.description = description;
        this.priceCents = priceCents;
        this.itemCondition = itemCondition;
        this.category = category;
        this.zone = zone;
        this.draftStep = draftStep;
    }

    public String getId() { return id; }
    public String getSellerId() { return sellerId; }
    public String getTitulo() { return title; }
    public String getDescripcion() { return description; }
    public String getCategoria() {
        if (category == null) return "Sin categoría";
        switch (category) {
            case "electronics": return "Tecnología";
            case "home": return "Hogar";
            case "fashion": return "Ropa y moda";
            case "sports": return "Deportes";
            case "vehicles": return "Vehículos";
            case "books": return "Libros";
            case "toys": return "Juguetes";
            case "other": return "Otros";
            default: return category;
        }
    }
    public String getEstado() {
        if (itemCondition == null) return "Sin especificar";
        switch (itemCondition) {
            case "new": return "Nuevo";
            case "like_new": return "Como nuevo";
            case "used": return "Usado";
            default: return itemCondition;
        }
    }
    public String getZona() { return zone; }
    public String getEstadoPublicacion() { return status; }
    public boolean isVisibleInPublicFeed() { return status == null || "active".equals(status); }
    public String getEstadoPublicacionVisible() {
        if (status == null) return "Sin estado";
        switch (status) {
            case "draft": return "Borrador";
            case "active": return "Activa";
            case "paused": return "Pausada";
            case "sold": return "Vendida";
            default: return status;
        }
    }
    public String getFechaPublicacion() { return publishedAt; }
    public String getCoverImage() { return coverImage; }
    public int getDraftStep() { return draftStep; }
    public double getPrecio() { return price != null ? price : priceCents != null ? priceCents / 100.0 : 0; }
    public int getFecha() {
        if (publishedAt == null) return 0;
        try { return (int) (OffsetDateTime.parse(publishedAt).toEpochSecond() / 86400); }
        catch (RuntimeException ignored) { return 0; }
    }
    public String getVendedorNombre() { return seller != null ? seller.getName() : sellerName; }
    public String getVendedorReputacion() {
        if (seller == null) return "Sin calificaciones";
        return String.format("%.1f (%d)", seller.getRatingAverage(), seller.getRatingCount());
    }
    public PublicUser getSeller() { return seller; }
    public List<String> getImagenes() {
        if (images == null) return Collections.emptyList();
        List<String> urls = new ArrayList<>();
        for (PublicationImage image : images) urls.add(image.getUrl());
        return urls;
    }
    public List<Question> getQuestions() { return questions != null ? questions : Collections.emptyList(); }
    public boolean isFavorite() { return Boolean.TRUE.equals(isFavorite); }
    public Actions getActions() { return actions; }

    public static class Question implements Serializable {
        private String id, text, answer, createdAt, answeredAt, askerId, askerName;
        public String getId() { return id; }
        public String getText() { return text; }
        public String getAnswer() { return answer; }
        public String getCreatedAt() { return createdAt; }
        public String getAnsweredAt() { return answeredAt; }
        public String getAskerId() { return askerId; }
        public String getAskerName() { return askerName; }
    }

    public static class Actions implements Serializable {
        private boolean canAsk, canOffer, canFavorite, canManage;
        public boolean canAsk() { return canAsk; }
        public boolean canOffer() { return canOffer; }
        public boolean canFavorite() { return canFavorite; }
        public boolean canManage() { return canManage; }
    }
}
