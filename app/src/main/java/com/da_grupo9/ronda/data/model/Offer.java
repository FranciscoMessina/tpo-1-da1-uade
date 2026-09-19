package com.da_grupo9.ronda.data.model;

/** Oferta devuelta por GET /me/offers. Los importes están en unidades de moneda. */
public class Offer {
    private String id;
    private String publicationId;
    private String title;
    private double amount;
    private Double counterAmount;
    private String message;
    private String status;
    private String role;
    private String expiresAt;
    private String createdAt;

    public String getId() { return id; }
    public String getPublicationId() { return publicationId; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public Double getCounterAmount() { return counterAmount; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getRole() { return role; }
    public String getExpiresAt() { return expiresAt; }
    public String getCreatedAt() { return createdAt; }
}
