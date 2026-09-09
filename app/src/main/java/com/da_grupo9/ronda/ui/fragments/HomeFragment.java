package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
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
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

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

    private String usuarioActualEmail = "";

    public HomeFragment() {
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

        if (getArguments() != null) {
            usuarioActualEmail =
                    getArguments().getString("email", "");
        }

        configurarSpinners();
        publicaciones = new ArrayList<>();
        publicacionesFiltradas = new ArrayList<>();
        cargarDatos();

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
        publicacionRepository.getPublicaciones(new PublicacionRepository.Resultado<List<Publicacion>>() {
            @Override
            public void onSuccess(List<Publicacion> data) {
                if (!isAdded()) return;
                publicaciones.clear();
                for (Publicacion publicacion : data) {
                    if (publicacion.isVisibleInPublicFeed()) {
                        publicaciones.add(publicacion);
                    }
                }
                publicacionesFiltradas = new ArrayList<>(publicaciones);
                paginaActual = 1;
                mostrarPagina();
            }

            @Override
            public void onError(String mensaje) {
                if (isAdded()) android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarSpinners() {

        String[] categorias = {
                "Todas",
                "Tecnología",
                "Hogar",
                "Deportes",
                "Ropa y moda",
                "Vehículos",
                "Libros",
                "Juguetes",
                "Otros"
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

        String texto =
                buscador.getText()
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
            precioMin =
                    Double.parseDouble(textoPrecioMinimo);
        }

        if (!textoPrecioMaximo.isEmpty()) {
            precioMax =
                    Double.parseDouble(textoPrecioMaximo);
        }

        List<Publicacion> resultados =
                new ArrayList<>();

        for (Publicacion publicacion : publicaciones) {

            boolean coincideTexto =
                    texto.isEmpty()
                            || (publicacion.getTitulo() != null && publicacion.getTitulo()
                            .toLowerCase()
                            .contains(texto))
                            || (publicacion.getDescripcion() != null && publicacion.getDescripcion()
                            .toLowerCase()
                            .contains(texto));

            boolean coincideCategoria =
                    categoriaSeleccionada.equals("Todas")
                            || publicacion.getCategoria()
                            .equals(categoriaSeleccionada);

            boolean coincideEstado =
                    estadoSeleccionado.equals("Todos")
                            || publicacion.getEstado()
                            .equals(estadoSeleccionado);

            boolean coincidePrecio =
                    publicacion.getPrecio() >= precioMin
                            && publicacion.getPrecio() <= precioMax;

            boolean coincideCercania =
                    cercaniaSeleccionada
                            .equals("Todas las zonas")
                            || "Palermo".equals(publicacion.getZona());

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
                                    p2.getFecha(),
                                    p1.getFecha()
                            )
            );

        } else if (ordenSeleccionado.equals("Menor precio")) {

            Collections.sort(
                    resultados,
                    (p1, p2) ->
                            Double.compare(
                                    p1.getPrecio(),
                                    p2.getPrecio()
                            )
            );

        } else if (ordenSeleccionado.equals("Mayor precio")) {

            Collections.sort(
                    resultados,
                    (p1, p2) ->
                            Double.compare(
                                    p2.getPrecio(),
                                    p1.getPrecio()
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

        int fin =
                Math.min(
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

            publicacionesContainer.addView(
                    crearMensajeVacio("No se encontraron publicaciones.")
            );

            return;
        }

        for (Publicacion publicacion : lista) {

            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(
            Publicacion publicacion) {

        MaterialCardView tarjeta = crearTarjeta();

        LinearLayout contenido =
                new LinearLayout(requireContext());

        contenido.setOrientation(LinearLayout.VERTICAL);
        tarjeta.addView(contenido);

        int colorOnSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        int colorOnSurfaceVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY);
        int colorPrice = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.price);
        int colorPrimary = MaterialColors.getColor(requireContext(), android.R.attr.colorPrimary, Color.BLUE);

        TextView titulo =
                new TextView(requireContext());

        titulo.setText(
                publicacion.getTitulo()
        );

        titulo.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        titulo.setTextColor(colorOnSurface);

        TextView descripcion =
                new TextView(requireContext());

        descripcion.setText(
                publicacion.getDescripcion()
        );

        descripcion.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        descripcion.setTextColor(colorOnSurfaceVariant);
        descripcion.setPaddingRelative(0, dpToPx(4), 0, 0);

        TextView precio =
                new TextView(requireContext());

        precio.setText(
                "Precio: $"
                        + publicacion.getPrecio()
        );

        precio.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
        precio.setTextColor(colorPrice);
        precio.setPaddingRelative(0, dpToPx(8), 0, 0);

        TextView estado =
                new TextView(requireContext());

        estado.setText(
                "Estado: "
                        + publicacion.getEstado()
        );

        estado.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        estado.setTextColor(colorOnSurfaceVariant);
        estado.setPaddingRelative(0, dpToPx(4), 0, 0);

        TextView categoria =
                new TextView(requireContext());

        categoria.setText(
                "Categoría: "
                        + publicacion.getCategoria()
        );

        categoria.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        categoria.setTextColor(colorOnSurfaceVariant);

        TextView zona =
                new TextView(requireContext());

        zona.setText(
                "Zona: "
                        + publicacion.getZona()
        );

        zona.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        zona.setTextColor(colorOnSurfaceVariant);

        TextView verDetalle =
                new TextView(requireContext());

        verDetalle.setText(
                "Ver detalle"
        );

        verDetalle.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge);
        verDetalle.setTextColor(colorPrimary);
        verDetalle.setPaddingRelative(
                0,
                dpToPx(12),
                0,
                0
        );

        contenido.addView(titulo);
        contenido.addView(descripcion);
        contenido.addView(precio);
        contenido.addView(estado);
        contenido.addView(categoria);
        contenido.addView(zona);
        contenido.addView(verDetalle);

        tarjeta.setOnClickListener(v -> {

            Bundle bundle =
                    new Bundle();

            bundle.putString(
                    "publicacionId",
                    publicacion.getId()
            );

            bundle.putString(
                    "usuarioActualEmail",
                    usuarioActualEmail
            );

            Navigation.findNavController(v)
                    .navigate(
                            R.id.action_homeFragment_to_detailFragment,
                            bundle
                    );
        });

        publicacionesContainer.addView(
                tarjeta
        );
    }

    private MaterialCardView crearTarjeta() {
        MaterialCardView tarjeta = new MaterialCardView(requireContext());

        tarjeta.setRadius(
                getResources().getDimension(R.dimen.corner_radius_card)
        );

        tarjeta.setCardElevation(
                getResources().getDimension(R.dimen.card_elevation)
        );

        tarjeta.setContentPadding(
                dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16)
        );

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dpToPx(12));
        tarjeta.setLayoutParams(parametros);

        return tarjeta;
    }

    private TextView crearMensajeVacio(String texto) {
        TextView mensaje = new TextView(requireContext());

        mensaje.setText(texto);
        mensaje.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        mensaje.setTextColor(
                MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY)
        );
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setPadding(dpToPx(16), dpToPx(32), dpToPx(16), dpToPx(32));

        return mensaje;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
