package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class ReviewsResponse {
    private List<ReviewItem> items;
    private PublicacionesResponse.Pagination pagination;

    public List<ReviewItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public PublicacionesResponse.Pagination getPagination() { return pagination; }
}
