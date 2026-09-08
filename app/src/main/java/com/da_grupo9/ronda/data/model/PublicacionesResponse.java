package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class PublicacionesResponse {
    private List<Publicacion> items;
    private Pagination pagination;

    public List<Publicacion> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public Pagination getPagination() { return pagination; }

    public static class Pagination {
        private int page;
        private int pageSize;
        private int total;
        private int totalPages;

        public int getPage() { return page; }
        public int getPageSize() { return pageSize; }
        public int getTotal() { return total; }
        public int getTotalPages() { return totalPages; }
    }
}
