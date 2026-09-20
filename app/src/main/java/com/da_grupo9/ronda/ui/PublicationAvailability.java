package com.da_grupo9.ronda.ui;

public final class PublicationAvailability {
    private PublicationAvailability() {}

    public static String unavailableLabel(String status) {
        if ("paused".equalsIgnoreCase(status)) return "PUBLICACIÓN PAUSADA";
        if ("sold".equalsIgnoreCase(status)) return "PUBLICACIÓN VENDIDA";
        return null;
    }
}
