package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class OperationsResponse {
    private List<Operation> items;

    public List<Operation> getItems() {
        return items != null ? items : Collections.emptyList();
    }
}
