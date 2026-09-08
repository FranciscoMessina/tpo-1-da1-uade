package com.da_grupo9.ronda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Publicacion implements Serializable {

    private int id;
    private String titulo;
    private String descripcion;
    private double precio;
    private String estado;
    private String categoria;
    private String zona;
    private int fecha; // orden numérico
    private String fechaPublicacion; // texto visible, ej: "15/08/2026"
    private String vendedorNombre;
    private String vendedorEmail;
    private String vendedorReputacion;
    private List<String> imagenes;
    private String estadoPublicacion;

    public Publicacion(
            int id,
            String titulo,
            String descripcion,
            double precio,
            String estado,
            String categoria,
            String zona,
            int fecha,
            String fechaPublicacion,
            String vendedorNombre,
            String vendedorEmail,
            String vendedorReputacion,
            List<String> imagenes) {

        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado;
        this.categoria = categoria;
        this.zona = zona;
        this.fecha = fecha;
        this.fechaPublicacion = fechaPublicacion;
        this.vendedorNombre = vendedorNombre;
        this.vendedorEmail = vendedorEmail;
        this.vendedorReputacion = vendedorReputacion;
        this.imagenes = imagenes != null ? imagenes : new ArrayList<>();
        this.estadoPublicacion = "Activa";
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public String getEstado() {
        return estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getZona() {
        return zona;
    }

    public int getFecha() {
        return fecha;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getVendedorNombre() {
        return vendedorNombre;
    }

    public String getVendedorEmail() {
        return vendedorEmail;
    }

    public String getVendedorReputacion() {
        return vendedorReputacion;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public void setEstadoPublicacion(String estadoPublicacion) {
        this.estadoPublicacion = estadoPublicacion;
    }
}
