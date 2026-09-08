package com.da_grupo9.ronda.data.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PublicUser implements Serializable {
    private String id, name, zone, memberSince;
    private double ratingAverage;
    private int ratingCount, purchasesCompleted, salesCompleted;
    private List<Publicacion> activePublications;
    public String getId() { return id; }
    public String getName() { return name; }
    public String getZone() { return zone; }
    public String getMemberSince() { return memberSince; }
    public double getRatingAverage() { return ratingAverage; }
    public int getRatingCount() { return ratingCount; }
    public int getPurchasesCompleted() { return purchasesCompleted; }
    public int getSalesCompleted() { return salesCompleted; }
    public List<Publicacion> getActivePublications() {
        return activePublications != null ? activePublications : Collections.emptyList();
    }
}
