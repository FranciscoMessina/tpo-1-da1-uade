package com.da_grupo9.ronda;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout publicacionesContainer;

    private EditText buscador;
    private EditText precioMinimo;
    private EditText precioMaximo;

    private Spinner spinnerCategoria;
    private Spinner spinnerEstado;
    private Spinner spinnerCercania;

    private Button botonFiltrar;

    private List<Publicacion> publicaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left + 16,
                    systemBars.top + 16,
                    systemBars.right + 16,
                    systemBars.bottom + 16
            );

            return insets;
        });

        buscador = findViewById(R.id.buscador);
        precioMinimo = findViewById(R.id.precioMinimo);
        precioMaximo = findViewById(R.id.precioMaximo);

        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        spinnerCercania = findViewById(R.id.spinnerCercania);

        botonFiltrar = findViewById(R.id.botonFiltrar);

        publicacionesContainer = findViewById(R.id.publicacionesContainer);

        cargarDatos();

        configurarSpinners();

        mostrarPublicaciones(publicaciones);

        botonFiltrar.setOnClickListener(view -> aplicarFiltros());
    }


    private void cargarDatos() {

        publicaciones = new ArrayList<>();

        publicaciones.add(new Publicacion(
                "iPhone 15 128GB",
                "iPhone 15 en excelente estado, batería al 95%.",
                850000,
                "Como nuevo",
                "Tecnología",
                "Palermo",
                5
        ));

        publicaciones.add(new Publicacion(
                "Bicicleta Mountain Bike",
                "Bicicleta rodado 29, ideal para ciudad y montaña.",
                350000,
                "Usado",
                "Deportes",
                "Belgrano",
                4
        ));

        publicaciones.add(new Publicacion(
                "PlayStation 5",
                "Consola PS5 con joystick original y poco uso.",
                900000,
                "Como nuevo",
                "Tecnología",
                "Caballito",
                3
        ));

        publicaciones.add(new Publicacion(
                "Monitor Samsung 24 pulgadas",
                "Monitor Full HD de 24 pulgadas funcionando perfectamente.",
                250000,
                "Usado",
                "Tecnología",
                "Recoleta",
                2
        ));

        publicaciones.add(new Publicacion(
                "Teclado mecánico Logitech",
                "Teclado mecánico RGB para gaming.",
                120000,
                "Nuevo",
                "Tecnología",
                "Villa Urquiza",
                1
        ));

        publicaciones.add(new Publicacion(
                "Zapatillas Nike",
                "Zapatillas deportivas nuevas, talle 42.",
                180000,
                "Nuevo",
                "Ropa",
                "Palermo",
                6
        ));
    }

    private void configurarSpinners() {

        String[] categorias = {
                "Todas",
                "Tecnología",
                "Deportes",
                "Ropa"
        };

        String[] estados = {
                "Todos",
                "Nuevo",
                "Como nuevo",
                "Usado"
        };

        String[] cercania = {
                "Todas las zonas",
                "Cerca de mí"
        };

        spinnerCategoria.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
        ));

        spinnerEstado.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                estados
        ));

        spinnerCercania.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cercania
        ));
    }

    private void aplicarFiltros() {

        String texto = buscador.getText().toString().toLowerCase().trim();

        String categoriaSeleccionada =
                spinnerCategoria.getSelectedItem().toString();

        String estadoSeleccionado =
                spinnerEstado.getSelectedItem().toString();

        String cercaniaSeleccionada =
                spinnerCercania.getSelectedItem().toString();

        String textoPrecioMinimo =
                precioMinimo.getText().toString().trim();

        String textoPrecioMaximo =
                precioMaximo.getText().toString().trim();

        double precioMin = 0;
        double precioMax = Double.MAX_VALUE;

        if (!textoPrecioMinimo.isEmpty()) {
            precioMin = Double.parseDouble(textoPrecioMinimo);
        }

        if (!textoPrecioMaximo.isEmpty()) {
            precioMax = Double.parseDouble(textoPrecioMaximo);
        }

        List<Publicacion> resultados = new ArrayList<>();

        for (Publicacion publicacion : publicaciones) {

            // Buscar por título o descripción
            boolean coincideTexto =
                    texto.isEmpty()
                            || publicacion.titulo.toLowerCase().contains(texto)
                            || publicacion.descripcion.toLowerCase().contains(texto);

            // Categoría
            boolean coincideCategoria =
                    categoriaSeleccionada.equals("Todas")
                            || publicacion.categoria.equals(categoriaSeleccionada);

            // Estado
            boolean coincideEstado =
                    estadoSeleccionado.equals("Todos")
                            || publicacion.estado.equals(estadoSeleccionado);

            // Precio
            boolean coincidePrecio =
                    publicacion.precio >= precioMin
                            && publicacion.precio <= precioMax;

            // Cercanía
            // En esta versión simulamos que la zona del usuario es Palermo.
            boolean coincideCercania =
                    cercaniaSeleccionada.equals("Todas las zonas")
                            || publicacion.zona.equals("Palermo");

            if (coincideTexto
                    && coincideCategoria
                    && coincideEstado
                    && coincidePrecio
                    && coincideCercania) {

                resultados.add(publicacion);
            }
        }

        mostrarPublicaciones(resultados);
    }

    private void mostrarPublicaciones(List<Publicacion> lista) {

        publicacionesContainer.removeAllViews();

        if (lista.isEmpty()) {

            TextView mensaje = new TextView(this);

            mensaje.setText("No se encontraron publicaciones.");
            mensaje.setTextSize(18);
            mensaje.setPadding(10, 20, 10, 20);

            publicacionesContainer.addView(mensaje);

            return;
        }

        for (Publicacion publicacion : lista) {

            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(Publicacion publicacion) {

        LinearLayout tarjeta = new LinearLayout(this);

        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(20, 20, 20, 20);
        tarjeta.setBackgroundColor(Color.LTGRAY);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, 16);

        tarjeta.setLayoutParams(parametros);

        TextView titulo = new TextView(this);
        titulo.setText(publicacion.titulo);
        titulo.setTextSize(20);
        titulo.setTextColor(Color.BLACK);

        TextView descripcion = new TextView(this);
        descripcion.setText(publicacion.descripcion);
        descripcion.setTextSize(15);
        descripcion.setTextColor(Color.DKGRAY);

        TextView precio = new TextView(this);
        precio.setText("Precio: $" + publicacion.precio);
        precio.setTextSize(18);
        precio.setTextColor(Color.BLACK);

        TextView estado = new TextView(this);
        estado.setText("Estado: " + publicacion.estado);
        estado.setTextSize(16);
        estado.setTextColor(Color.DKGRAY);

        TextView categoria = new TextView(this);
        categoria.setText("Categoría: " + publicacion.categoria);
        categoria.setTextSize(16);
        categoria.setTextColor(Color.DKGRAY);

        TextView zona = new TextView(this);
        zona.setText("Zona: " + publicacion.zona);
        zona.setTextSize(16);
        zona.setTextColor(Color.DKGRAY);

        tarjeta.addView(titulo);
        tarjeta.addView(descripcion);
        tarjeta.addView(precio);
        tarjeta.addView(estado);
        tarjeta.addView(categoria);
        tarjeta.addView(zona);

        publicacionesContainer.addView(tarjeta);
    }

    private static class Publicacion {

        String titulo;
        String descripcion;
        double precio;
        String estado;
        String categoria;
        String zona;
        int fecha;

        public Publicacion(
                String titulo,
                String descripcion,
                double precio,
                String estado,
                String categoria,
                String zona,
                int fecha) {

            this.titulo = titulo;
            this.descripcion = descripcion;
            this.precio = precio;
            this.estado = estado;
            this.categoria = categoria;
            this.zona = zona;
            this.fecha = fecha;
        }
    }
}
