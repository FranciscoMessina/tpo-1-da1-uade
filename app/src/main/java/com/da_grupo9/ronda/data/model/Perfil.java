package com.da_grupo9.ronda.data.model;

public class Perfil {
    private String id, email, username, name, phone, zone, createdAt;
    private PublicUser reputation;

    public Perfil(String name, String email, String phone, String zone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.zone = zone;
    }
    public String getId() { return id; }
    public String getNombre() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getTelefono() { return phone; }
    public String getZona() { return zone; }
    public String getCreatedAt() { return createdAt; }
    public PublicUser getReputation() { return reputation; }
}
