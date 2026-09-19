package com.da_grupo9.ronda.data.model;

public class Operation {
    private String id, completedAt, publicationId, title, address, sellerId, sellerName, type, buyerId;
    private Integer myRating;
    private double amount;

    public String getId() { return id; }
    public String getType() { return type; }
    public Integer getMyRating() { return myRating; }
    public double getAmount() { return amount; }
}
