package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class MisPublicacionesFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

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

        publicacionRepository.getMisPublicaciones(new PublicacionRepository.Resultado<List<Publicacion>>() {
            @Override public void onSuccess(List<Publicacion> publicaciones) {
                if (!isAdded()) return;
                renderizarPublicaciones(publicaciones);
            }
            @Override public void onError(String mensaje) {
                if (!isAdded()) return;
                TextView error = new TextView(requireContext());
                error.setText(mensaje);
                publicacionesContainer.addView(error);
            }
        });
    }

    private void renderizarPublicaciones(List<Publicacion> publicaciones) {
        publicacionesContainer.removeAllViews();
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
        estado.setText("Estado de publicación: " + publicacion.getEstadoPublicacionVisible());
        estado.setTextSize(16);
        estado.setTextColor(Color.DKGRAY);

        tarjeta.addView(titulo);
        tarjeta.addView(precio);
        tarjeta.addView(estado);

        if ("active".equals(publicacion.getEstadoPublicacion())) {
            Button buttonPausar = new Button(requireContext());
            buttonPausar.setText("Pausar");
            buttonPausar.setOnClickListener(v -> {
                publicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "paused",
                        recargarAlFinalizar()
                );
            });
            tarjeta.addView(buttonPausar);
        } else if ("paused".equals(publicacion.getEstadoPublicacion())) {
            Button buttonReactivar = new Button(requireContext());
            buttonReactivar.setText("Reactivar");
            buttonReactivar.setOnClickListener(v -> {
                publicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "active",
                        recargarAlFinalizar()
                );
            });
            tarjeta.addView(buttonReactivar);
        }

        publicacionesContainer.addView(tarjeta);
    }

    private PublicacionRepository.Resultado<Publicacion> recargarAlFinalizar() {
        return new PublicacionRepository.Resultado<Publicacion>() {
            @Override public void onSuccess(Publicacion data) { mostrarPublicaciones(); }
            @Override public void onError(String mensaje) {
                if (isAdded()) android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            }
        };
    }
}
