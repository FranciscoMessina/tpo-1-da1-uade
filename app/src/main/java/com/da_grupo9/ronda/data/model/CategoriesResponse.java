package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class CategoriesResponse {
    private List<String> items;

    public List<String> getItems() {
        return items != null ? items : Collections.emptyList();
    }
}
