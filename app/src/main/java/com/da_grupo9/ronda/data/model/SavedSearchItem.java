package com.da_grupo9.ronda.data.model;

public class SavedSearchItem {

    private String id;
    private String name;
    private String query;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String condition;
    private String zone;
    private String sort;
    private int unreadCount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public String getCategory() {
        return category;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public String getCondition() {
        return condition;
    }

    public String getZone() {
        return zone;
    }

    public String getSort() {
        return sort;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}