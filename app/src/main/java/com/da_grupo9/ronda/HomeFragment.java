package com.da_grupo9.ronda;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private LinearLayout publicacionesContainer;

    private EditText buscador;
    private EditText precioMinimo;
    private EditText precioMaximo;

    private Spinner spinnerCategoria;
    private Spinner spinnerEstado;
    private Spinner spinnerCercania;
    private Spinner spinnerOrden;

    private Button botonFiltrar;
    private Button botonAnterior;
    private Button botonSiguiente;

    private TextView textoPagina;

    private List<Publicacion> publicaciones;
    private List<Publicacion> publicacionesFiltradas;

    private int paginaActual = 1;
    private final int publicacionesPorPagina = 3;

    public HomeFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        buscador = view.findViewById(R.id.buscador);
        precioMinimo = view.findViewById(R.id.precioMinimo);
        precioMaximo = view.findViewById(R.id.precioMaximo);

        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        spinnerCercania = view.findViewById(R.id.spinnerCercania);
        spinnerOrden = view.findViewById(R.id.spinnerOrden);

        botonFiltrar = view.findViewById(R.id.botonFiltrar);
        botonAnterior = view.findViewById(R.id.botonAnterior);
        botonSiguiente = view.findViewById(R.id.botonSiguiente);

        textoPagina = view.findViewById(R.id.textoPagina);

        publicacionesContainer =
                view.findViewById(R.id.publicacionesContainer);

        cargarDatos();

        configurarSpinners();

        publicacionesFiltradas =
                new ArrayList<>(publicaciones);

        mostrarPagina();

        botonFiltrar.setOnClickListener(
                v -> aplicarFiltros()
        );

        botonAnterior.setOnClickListener(
                v -> paginaAnterior()
        );

        botonSiguiente.setOnClickListener(
                v -> paginaSiguiente()
        );
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

        String[] ordenamientos = {
                "Más recientes",
                "Menor precio",
                "Mayor precio"
        };

        spinnerCategoria.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categorias
                )
        );

        spinnerEstado.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        estados
                )
        );

        spinnerCercania.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        cercania
                )
        );

        spinnerOrden.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        ordenamientos
                )
        );
    }

    private void aplicarFiltros() {

        String texto = buscador.getText()
                .toString()
                .toLowerCase()
                .trim();

        String categoriaSeleccionada =
                spinnerCategoria.getSelectedItem().toString();

        String estadoSeleccionado =
                spinnerEstado.getSelectedItem().toString();

        String cercaniaSeleccionada =
                spinnerCercania.getSelectedItem().toString();

        String ordenSeleccionado =
                spinnerOrden.getSelectedItem().toString();

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

        List<Publicacion> resultados =
                new ArrayList<>();

        for (Publicacion publicacion : publicaciones) {

            boolean coincideTexto =
                    texto.isEmpty()
                            || publicacion.titulo
                            .toLowerCase()
                            .contains(texto)
                            || publicacion.descripcion
                            .toLowerCase()
                            .contains(texto);

            boolean coincideCategoria =
                    categoriaSeleccionada.equals("Todas")
                            || publicacion.categoria
                            .equals(categoriaSeleccionada);

            boolean coincideEstado =
                    estadoSeleccionado.equals("Todos")
                            || publicacion.estado
                            .equals(estadoSeleccionado);

            boolean coincidePrecio =
                    publicacion.precio >= precioMin
                            && publicacion.precio <= precioMax;

            boolean coincideCercania =
                    cercaniaSeleccionada
                            .equals("Todas las zonas")
                            || publicacion.zona
                            .equals("Palermo");

            if (coincideTexto
                    && coincideCategoria
                    && coincideEstado
                    && coincidePrecio
                    && coincideCercania) {

                resultados.add(publicacion);
            }
        }

        if (ordenSeleccionado.equals("Más recientes")) {

            Collections.sort(
                    resultados,
                    (p1, p2) ->
                            Integer.compare(
                                    p2.fecha,
                                    p1.fecha
                            )
            );

        } else if (ordenSeleccionado.equals("Menor precio")) {

            Collections.sort(
                    resultados,
                    (p1, p2) ->
                            Double.compare(
                                    p1.precio,
                                    p2.precio
                            )
            );

        } else if (ordenSeleccionado.equals("Mayor precio")) {

            Collections.sort(
                    resultados,
                    (p1, p2) ->
                            Double.compare(
                                    p2.precio,
                                    p1.precio
                            )
            );
        }

        publicacionesFiltradas = resultados;

        paginaActual = 1;

        mostrarPagina();
    }

    private void mostrarPagina() {

        if (publicacionesFiltradas.isEmpty()) {

            mostrarPublicaciones(
                    publicacionesFiltradas
            );

            paginaActual = 1;

            textoPagina.setText(
                    "Página 1 de 1"
            );

            botonAnterior.setEnabled(false);
            botonSiguiente.setEnabled(false);

            return;
        }

        int inicio =
                (paginaActual - 1)
                        * publicacionesPorPagina;

        int fin = Math.min(
                inicio + publicacionesPorPagina,
                publicacionesFiltradas.size()
        );

        List<Publicacion> publicacionesPagina =
                publicacionesFiltradas.subList(
                        inicio,
                        fin
                );

        mostrarPublicaciones(
                publicacionesPagina
        );

        int totalPaginas =
                (int) Math.ceil(
                        (double)
                                publicacionesFiltradas.size()
                                / publicacionesPorPagina
                );

        textoPagina.setText(
                "Página "
                        + paginaActual
                        + " de "
                        + totalPaginas
        );

        botonAnterior.setEnabled(
                paginaActual > 1
        );

        botonSiguiente.setEnabled(
                paginaActual < totalPaginas
        );
    }

    private void paginaAnterior() {

        if (paginaActual > 1) {

            paginaActual--;

            mostrarPagina();
        }
    }

    private void paginaSiguiente() {

        int totalPaginas =
                (int) Math.ceil(
                        (double)
                                publicacionesFiltradas.size()
                                / publicacionesPorPagina
                );

        if (paginaActual < totalPaginas) {

            paginaActual++;

            mostrarPagina();
        }
    }

    private void mostrarPublicaciones(
            List<Publicacion> lista) {

        publicacionesContainer.removeAllViews();

        if (lista.isEmpty()) {

            TextView mensaje =
                    new TextView(requireContext());

            mensaje.setText(
                    "No se encontraron publicaciones."
            );

            mensaje.setTextSize(18);

            mensaje.setPadding(
                    10,
                    20,
                    10,
                    20
            );

            publicacionesContainer.addView(
                    mensaje
            );

            return;
        }

        for (Publicacion publicacion : lista) {

            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(
            Publicacion publicacion) {

        LinearLayout tarjeta =
                new LinearLayout(requireContext());

        tarjeta.setOrientation(
                LinearLayout.VERTICAL
        );

        tarjeta.setPadding(
                20,
                20,
                20,
                20
        );

        tarjeta.setBackgroundColor(
                Color.LTGRAY
        );

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(
                0,
                0,
                0,
                16
        );

        tarjeta.setLayoutParams(parametros);

        TextView titulo =
                new TextView(requireContext());

        titulo.setText(publicacion.titulo);
        titulo.setTextSize(20);
        titulo.setTextColor(Color.BLACK);

        TextView descripcion =
                new TextView(requireContext());

        descripcion.setText(
                publicacion.descripcion
        );

        descripcion.setTextSize(15);
        descripcion.setTextColor(Color.DKGRAY);

        TextView precio =
                new TextView(requireContext());

        precio.setText(
                "Precio: $" + publicacion.precio
        );

        precio.setTextSize(18);
        precio.setTextColor(Color.BLACK);

        TextView estado =
                new TextView(requireContext());

        estado.setText(
                "Estado: " + publicacion.estado
        );

        estado.setTextSize(16);
        estado.setTextColor(Color.DKGRAY);

        TextView categoria =
                new TextView(requireContext());

        categoria.setText(
                "Categoría: "
                        + publicacion.categoria
        );

        categoria.setTextSize(16);
        categoria.setTextColor(Color.DKGRAY);

        TextView zona =
                new TextView(requireContext());

        zona.setText(
                "Zona: " + publicacion.zona
        );

        zona.setTextSize(16);
        zona.setTextColor(Color.DKGRAY);

        tarjeta.addView(titulo);
        tarjeta.addView(descripcion);
        tarjeta.addView(precio);
        tarjeta.addView(estado);
        tarjeta.addView(categoria);
        tarjeta.addView(zona);

        publicacionesContainer.addView(
                tarjeta
        );
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