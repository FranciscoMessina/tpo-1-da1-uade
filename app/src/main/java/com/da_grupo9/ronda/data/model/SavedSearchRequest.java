package com.da_grupo9.ronda.data.model;

public class SavedSearchRequest {

    private String name;
    private String query;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String condition;
    private String zone;
    private String sort;

    public SavedSearchRequest(
            String name,
            String query,
            String category,
            Double minPrice,
            Double maxPrice,
            String condition,
            String zone,
            String sort) {

        this.name = name;
        this.query = query;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.condition = condition;
        this.zone = zone;
        this.sort = sort;
    }
}