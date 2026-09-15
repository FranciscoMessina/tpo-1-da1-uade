package com.da_grupo9.ronda.data.model;

import java.io.Serializable;

public class PublicationImage implements Serializable {
    private String id;
    private String url;
    private int position;

    public PublicationImage() {}

    public PublicationImage(String id, String url, int position) {
        this.id = id;
        this.url = url;
        this.position = position;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public int getPosition() { return position; }
}
