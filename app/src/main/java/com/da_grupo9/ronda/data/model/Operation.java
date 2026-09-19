package com.da_grupo9.ronda.data.model;

import java.io.Serializable;

public class Operation implements Serializable {
    public static final String TYPE_BUY = "buy";
    public static final String TYPE_SELL = "sell";

    private String id, completedAt, publicationId, title, address, type;
    private String sellerId, sellerName, buyerId, buyerName;
    private String counterpartyId, counterpartyName, counterpartyAvatarUrl, reviewDeadline;
    private Integer myRating;
    private Boolean canReview;
    private double amount;

    public String getId() { return id; }
    public String getCompletedAt() { return completedAt; }
    public String getPublicationId() { return publicationId; }
    public String getTitle() { return title; }
    public String getAddress() { return address; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getType() { return type; }
    public String getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public String getCounterpartyId() { return counterpartyId; }
    public String getCounterpartyName() { return counterpartyName; }
    public String getCounterpartyAvatarUrl() { return counterpartyAvatarUrl; }
    public String getReviewDeadline() { return reviewDeadline; }
    public Integer getMyRating() { return myRating; }
    public double getAmount() { return amount; }

    public boolean isPurchase() { return TYPE_BUY.equals(type); }

    /** El servidor decide si se puede calificar (sin calificación previa y dentro de los 7 días). */
    public boolean canRate() { return Boolean.TRUE.equals(canReview); }

    /** Refleja localmente una calificación recién enviada, sin esperar a recargar. */
    public void setMyRating(int rating) {
        this.myRating = rating;
        this.canReview = false;
    }
}
