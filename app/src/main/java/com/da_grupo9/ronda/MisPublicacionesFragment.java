package com.da_grupo9.ronda;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

public class MisPublicacionesFragment extends Fragment {

    private LinearLayout publicacionesContainer;
    private String email = "";

    public MisPublicacionesFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_mis_publicaciones,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        publicacionesContainer =
                view.findViewById(R.id.misPublicacionesContainer);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        mostrarPublicaciones();
    }

    private void mostrarPublicaciones() {
        publicacionesContainer.removeAllViews();

        List<Publicacion> publicaciones =
                PublicacionRepository.getPublicacionesPorVendedor(email);

        if (publicaciones.isEmpty()) {
            TextView mensaje = new TextView(requireContext());
            mensaje.setText("Todavía no tenés publicaciones.");
            mensaje.setTextSize(18);
            publicacionesContainer.addView(mensaje);
            return;
        }

        for (Publicacion publicacion : publicaciones) {
            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(Publicacion publicacion) {
        LinearLayout tarjeta = new LinearLayout(requireContext());
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

        TextView titulo = new TextView(requireContext());
        titulo.setText(publicacion.getTitulo());
        titulo.setTextSize(20);
        titulo.setTextColor(Color.BLACK);

        TextView precio = new TextView(requireContext());
        precio.setText("Precio: $" + publicacion.getPrecio());
        precio.setTextSize(16);
        precio.setTextColor(Color.DKGRAY);

        TextView estado = new TextView(requireContext());
        estado.setText("Estado de publicación: " + publicacion.getEstadoPublicacion());
        estado.setTextSize(16);
        estado.setTextColor(Color.DKGRAY);

        tarjeta.addView(titulo);
        tarjeta.addView(precio);
        tarjeta.addView(estado);

        if (publicacion.getEstadoPublicacion().equals("Activa")) {
            Button buttonPausar = new Button(requireContext());
            buttonPausar.setText("Pausar");
            buttonPausar.setOnClickListener(v -> {
                PublicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "Pausada"
                );
                mostrarPublicaciones();
            });
            tarjeta.addView(buttonPausar);
        } else if (publicacion.getEstadoPublicacion().equals("Pausada")) {
            Button buttonReactivar = new Button(requireContext());
            buttonReactivar.setText("Reactivar");
            buttonReactivar.setOnClickListener(v -> {
                PublicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "Activa"
                );
                mostrarPublicaciones();
            });
            tarjeta.addView(buttonReactivar);
        }

        publicacionesContainer.addView(tarjeta);
    }
}
