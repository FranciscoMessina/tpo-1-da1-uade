package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class PublicProfileFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

    public PublicProfileFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_public_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView textNombre = view.findViewById(R.id.textPerfilPublicoNombre);
        TextView textEmail = view.findViewById(R.id.textPerfilPublicoEmail);
        TextView textReputacion = view.findViewById(R.id.textPerfilPublicoReputacion);
        TextView textAntiguedad = view.findViewById(R.id.textPerfilPublicoAntiguedad);

        LinearLayout containerPublicaciones =
                view.findViewById(R.id.containerPublicacionesVendedor);

        Button buttonVolver =
                view.findViewById(R.id.buttonVolverPerfilPublico);

        String vendedorNombre = "";
        String vendedorEmail = "";
        String vendedorReputacion = "";

        if (getArguments() != null) {
            vendedorNombre = getArguments().getString("vendedorNombre", "");
            vendedorEmail = getArguments().getString("vendedorEmail", "");
            vendedorReputacion = getArguments().getString("vendedorReputacion", "");
        }

        textNombre.setText(vendedorNombre);
        textEmail.setText(vendedorEmail);
        textReputacion.setText("Reputación: " + vendedorReputacion);

        textAntiguedad.setText("Antigüedad en la plataforma: 2 años");

        cargarPublicacionesActivas(vendedorEmail, containerPublicaciones);

        buttonVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void cargarPublicacionesActivas(
            String vendedorEmail,
            LinearLayout container) {

        publicacionRepository.getPublicacionesPorVendedor(vendedorEmail, new PublicacionRepository.Resultado<List<Publicacion>>() {
            @Override public void onSuccess(List<Publicacion> publicaciones) {
                if (isAdded()) renderizarPublicacionesActivas(publicaciones, container);
            }
            @Override public void onError(String mensaje) {
                if (!isAdded()) return;
                TextView error = new TextView(requireContext());
                error.setText(mensaje);
                container.addView(error);
            }
        });
    }

    private void renderizarPublicacionesActivas(List<Publicacion> publicaciones, LinearLayout container) {
        int cantidad = 0;

        for (Publicacion publicacion : publicaciones) {

            boolean estaActiva =
                    publicacion.getEstadoPublicacion()
                            .equalsIgnoreCase("Activa");

            if (estaActiva) {

                TextView publicacionView =
                        new TextView(requireContext());

                publicacionView.setText(
                        "• "
                                + publicacion.getTitulo()
                                + " - $"
                                + String.format("%,.0f", publicacion.getPrecio())
                );

                publicacionView.setTextSize(16);
                publicacionView.setPadding(0, 10, 0, 10);

                container.addView(publicacionView);

                cantidad++;
            }
        }

        if (cantidad == 0) {

            TextView mensaje =
                    new TextView(requireContext());

            mensaje.setText(
                    "El usuario no tiene publicaciones activas."
            );

            mensaje.setTextSize(16);

            container.addView(mensaje);
        }
    }
}
