package com.da_grupo9.ronda.data.model;

public class Perfil {
    private String id, email, username, name, phone, zone, avatarUrl, createdAt;
    private PublicUser reputation;
    private Boolean hasPassword;

    public Perfil(String name, String email, String phone, String zone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.zone = zone;
    }
    public Perfil(String name, String email, String phone, String zone, String avatarUrl) {
        this(name, email, phone, zone);
        this.avatarUrl = avatarUrl;
    }
    public String getId() { return id; }
    public String getNombre() { return name; }
    public String getEmail() { return email; }
    public String getTelefono() { return phone; }
    public String getZona() { return zone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCreatedAt() { return createdAt; }
    public PublicUser getReputation() { return reputation; }
    public Boolean getHasPassword() { return hasPassword; }
}
