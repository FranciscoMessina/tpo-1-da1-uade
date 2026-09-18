package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class FavoritesResponse {

    private List<FavoriteItem> items;
    private int unreadCount;

    public List<FavoriteItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}