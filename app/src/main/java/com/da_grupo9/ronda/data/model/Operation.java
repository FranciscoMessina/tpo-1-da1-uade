package com.da_grupo9.ronda.data.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

public class Operation implements Serializable {
    public static final String TYPE_BUY = "buy";
    public static final String TYPE_SELL = "sell";
    private static final Duration RATING_WINDOW = Duration.ofDays(7);

    private String id, completedAt, publicationId, title, address, sellerId, sellerName, type, buyerId;
    private Integer myRating;
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
    public Integer getMyRating() { return myRating; }
    public double getAmount() { return amount; }

    public void setMyRating(Integer myRating) { this.myRating = myRating; }

    public boolean isPurchase() { return TYPE_BUY.equals(type); }

    /** Id de la otra parte: el vendedor en una compra, el comprador en una venta. */
    public String getCounterpartyId() { return isPurchase() ? sellerId : buyerId; }

    /** El backend sólo informa el nombre del vendedor; en una venta la contraparte no trae nombre. */
    public String getCounterpartyName() { return isPurchase() ? sellerName : null; }

    /**
     * Regla provisoria hasta que el backend informe el permiso: sin calificación previa y dentro
     * de los 7 días posteriores a la operación. El backend igualmente valida la regla.
     */
    public boolean canRate() {
        if (myRating != null || completedAt == null) return false;
        try {
            return !OffsetDateTime.now().isAfter(OffsetDateTime.parse(completedAt).plus(RATING_WINDOW));
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
