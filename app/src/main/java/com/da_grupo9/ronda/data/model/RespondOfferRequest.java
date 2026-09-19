package com.da_grupo9.ronda.data.model;

public class RespondOfferRequest {
    private final String action;
    private final Double counterAmount;

    public RespondOfferRequest(String action) { this(action, null); }

    public RespondOfferRequest(String action, Double counterAmount) {
        this.action = action;
        this.counterAmount = counterAmount;
    }

    public String getAction() { return action; }
    public Double getCounterAmount() { return counterAmount; }
}
