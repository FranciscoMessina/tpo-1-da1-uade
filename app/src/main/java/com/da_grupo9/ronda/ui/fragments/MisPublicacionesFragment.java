package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                publicacionesContainer.addView(crearMensajeVacio(mensaje));
            }
        });
    }

    private void renderizarPublicaciones(List<Publicacion> publicaciones) {
        publicacionesContainer.removeAllViews();
        if (publicaciones.isEmpty()) {
            publicacionesContainer.addView(crearMensajeVacio("Todavía no tenés publicaciones."));
            return;
        }

        for (Publicacion publicacion : publicaciones) {
            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(Publicacion publicacion) {
        MaterialCardView tarjeta = crearTarjeta();

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);
        tarjeta.addView(contenido);

        int colorOnSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK);
        int colorOnSurfaceVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY);
        int colorPrice = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.price);

        TextView titulo = new TextView(requireContext());
        titulo.setText(publicacion.getTitulo());
        titulo.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        titulo.setTextColor(colorOnSurface);

        TextView precio = new TextView(requireContext());
        precio.setText("Precio: $" + publicacion.getPrecio());
        precio.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
        precio.setTextColor(colorPrice);
        precio.setPaddingRelative(0, dpToPx(4), 0, 0);

        TextView estado = new TextView(requireContext());
        estado.setText("Estado de publicación: " + publicacion.getEstadoPublicacionVisible());
        estado.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        estado.setTextColor(colorOnSurfaceVariant);
        estado.setPaddingRelative(0, dpToPx(4), 0, 0);

        contenido.addView(titulo);
        contenido.addView(precio);
        contenido.addView(estado);

        if ("active".equals(publicacion.getEstadoPublicacion())) {
            MaterialButton buttonPausar = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            buttonPausar.setText("Pausar");
            buttonPausar.setIcon(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause));
            buttonPausar.setLayoutParams(botonAccionParams());
            buttonPausar.setOnClickListener(v -> {
                publicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "paused",
                        recargarAlFinalizar()
                );
            });
            contenido.addView(buttonPausar);
        } else if ("paused".equals(publicacion.getEstadoPublicacion())) {
            MaterialButton buttonReactivar = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            buttonReactivar.setText("Reactivar");
            buttonReactivar.setIcon(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_arrow));
            buttonReactivar.setLayoutParams(botonAccionParams());
            buttonReactivar.setOnClickListener(v -> {
                publicacionRepository.cambiarEstadoPublicacion(
                        publicacion.getId(),
                        "active",
                        recargarAlFinalizar()
                );
            });
            contenido.addView(buttonReactivar);
        }

        publicacionesContainer.addView(tarjeta);
    }

    private LinearLayout.LayoutParams botonAccionParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(12);
        return params;
    }

    private MaterialCardView crearTarjeta() {
        MaterialCardView tarjeta = new MaterialCardView(requireContext());

        tarjeta.setRadius(getResources().getDimension(R.dimen.corner_radius_card));
        tarjeta.setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        tarjeta.setContentPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
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

    private PublicacionRepository.Resultado<Publicacion> recargarAlFinalizar() {
        return new PublicacionRepository.Resultado<Publicacion>() {
            @Override public void onSuccess(Publicacion data) { mostrarPublicaciones(); }
            @Override public void onError(String mensaje) {
                if (isAdded()) android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            }
        };
    }
}
