package com.da_grupo9.ronda.data.model;

import java.util.Collections;
import java.util.List;

public class SavedSearchesResponse {

    private List<SavedSearchItem> items;
    private int unreadCount;

    public List<SavedSearchItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}