package com.da_grupo9.ronda.data.model;

/** Cuerpo enviado al crear una oferta sobre una publicación. */
public class CreateOfferRequest {
    private final double amount;
    private final String message;

    public CreateOfferRequest(double amount, String message) {
        this.amount = amount;
        this.message = message;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
