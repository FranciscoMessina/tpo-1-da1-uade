package com.da_grupo9.ronda.data.model;

import java.io.Serializable;

public class PublicationImage implements Serializable {
    private String id;
    private String url;
    private int position;
    public String getId() { return id; }
    public String getUrl() { return url; }
    public int getPosition() { return position; }
}
