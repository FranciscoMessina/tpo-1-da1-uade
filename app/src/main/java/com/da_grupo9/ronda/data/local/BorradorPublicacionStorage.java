package com.da_grupo9.ronda.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorradorPublicacionStorage {

    private static final String PREFS_NAME = "borrador_publicacion";
    private static final String SEPARADOR_FOTOS = "\n";

    private final SharedPreferences preferences;

    public BorradorPublicacionStorage(Context context, String email) {
        String usuario = email == null || email.trim().isEmpty()
                ? "sin_usuario"
                : String.valueOf(email.trim().toLowerCase(Locale.ROOT).hashCode());
        preferences = context.getSharedPreferences(PREFS_NAME + "_" + usuario, Context.MODE_PRIVATE);
    }

    public void guardar(Borrador borrador) {
        preferences.edit()
                .putString("titulo", borrador.titulo)
                .putString("descripcion", borrador.descripcion)
                .putString("precio", borrador.precio)
                .putString("zona", borrador.zona)
                .putString("direccion", borrador.direccion)
                .putString("latitud", borrador.latitud)
                .putString("longitud", borrador.longitud)
                .putInt("categoria", borrador.categoria)
                .putInt("estado", borrador.estado)
                .putInt("paso", borrador.paso)
                .putString("fotos", String.join(SEPARADOR_FOTOS, borrador.fotos))
                .putBoolean("guardado", true)
                .apply();
    }

    public Borrador cargar() {
        if (!preferences.getBoolean("guardado", false)) {
            return null;
        }

        Borrador borrador = new Borrador();
        borrador.titulo = preferences.getString("titulo", "");
        borrador.descripcion = preferences.getString("descripcion", "");
        borrador.precio = preferences.getString("precio", "");
        borrador.zona = preferences.getString("zona", "");
        borrador.direccion = preferences.getString("direccion", "");
        borrador.latitud = preferences.getString("latitud", "");
        borrador.longitud = preferences.getString("longitud", "");
        borrador.categoria = preferences.getInt("categoria", 0);
        borrador.estado = preferences.getInt("estado", 0);
        borrador.paso = preferences.getInt("paso", 1);

        String fotosGuardadas = preferences.getString("fotos", "");
        if (!fotosGuardadas.isEmpty()) {
            String[] rutas = fotosGuardadas.split(SEPARADOR_FOTOS);
            for (String ruta : rutas) {
                if (new File(ruta).exists()) {
                    borrador.fotos.add(ruta);
                }
            }
        }

        return borrador;
    }

    public void borrar() {
        Borrador borrador = cargar();
        if (borrador != null) {
            for (String ruta : borrador.fotos) {
                new File(ruta).delete();
            }
        }
        preferences.edit().clear().apply();
    }

    public static class Borrador {
        public String titulo = "";
        public String descripcion = "";
        public String precio = "";
        public String zona = "";
        public String direccion = "";
        public String latitud = "";
        public String longitud = "";
        public int categoria;
        public int estado;
        public int paso = 1;
        public List<String> fotos = new ArrayList<>();
    }
}
