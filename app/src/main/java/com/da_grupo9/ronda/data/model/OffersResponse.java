package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class OffersResponse {
    private List<Offer> items;

    public List<Offer> getItems() {
        return items == null ? Collections.emptyList() : items;
    }
}
