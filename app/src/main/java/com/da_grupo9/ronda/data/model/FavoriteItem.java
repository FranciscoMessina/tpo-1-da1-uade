package com.da_grupo9.ronda.data.model;

public class FavoriteItem {

    private String id;
    private String publicationId;
    private String title;
    private double price;
    private String itemCondition;
    private String zone;
    private String status;
    private boolean hasUpdate;
    private String favoritedAt;
    private int priceChanged;
    private String coverImage;

    public String getId() {
        return id;
    }

    public String getPublicationId() {
        return publicationId != null && !publicationId.isEmpty() ? publicationId : id;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public String getZone() {
        return zone;
    }

    public String getStatus() {
        return status;
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public boolean hasPriceChanged() {
        return priceChanged == 1;
    }

    public String getCoverImage() {
        return coverImage;
    }
}
