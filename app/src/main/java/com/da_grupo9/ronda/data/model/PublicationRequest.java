package com.da_grupo9.ronda.data.model;

import java.util.List;

/** Cuerpo aceptado por POST/PATCH /publications en la API 1.0.50. */
public class PublicationRequest {
    private final String title;
    private final String description;
    private final String category;
    private final Double price;
    private final String condition;
    private final String address;
    private final List<String> imageUrls;

    public PublicationRequest(String title, String description, String category, double price,
                              String condition, String address, List<String> imageUrls) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.condition = condition;
        this.address = address;
        this.imageUrls = imageUrls;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public String getAddress() { return address; }
}
