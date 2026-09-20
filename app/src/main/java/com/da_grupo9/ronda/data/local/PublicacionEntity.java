package com.da_grupo9.ronda.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** Caché por cuenta: el mismo artículo puede tener datos personalizados distintos según quién lo consulta. */
@Entity(tableName = "publicaciones", primaryKeys = {"accountId", "id"})
public class PublicacionEntity {

    /** Id de la cuenta que obtuvo estos datos; vacío para consultas sin sesión. */
    @NonNull
    private String accountId = "";

    @NonNull
    private String id = "";

    private String title;
    private String description;
    private String category;
    private double price;
    private String itemCondition;
    private String zone;
    private String status;
    private String sellerName;
    private String fullJson;
    /** Verdadero si fullJson proviene del detalle y no sólo del resumen del feed. */
    private boolean hasDetail;
    private long lastConsultedAt;
    private long cachedAt;

    public PublicacionEntity() {
    }

    @NonNull
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(@NonNull String accountId) {
        this.accountId = accountId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getFullJson() {
        return fullJson;
    }

    public void setFullJson(String fullJson) {
        this.fullJson = fullJson;
    }

    public boolean isHasDetail() {
        return hasDetail;
    }

    public void setHasDetail(boolean hasDetail) {
        this.hasDetail = hasDetail;
    }

    public long getLastConsultedAt() {
        return lastConsultedAt;
    }

    public void setLastConsultedAt(long lastConsultedAt) {
        this.lastConsultedAt = lastConsultedAt;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
