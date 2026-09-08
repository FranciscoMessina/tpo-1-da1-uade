package com.da_grupo9.ronda.data.model;

public class Perfil {
    private String nombre;
    private String email;
    private String telefono;
    private String zona;

    public Perfil(String nombre, String email, String telefono, String zona) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.zona = zona;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getZona() { return zona; }
}
