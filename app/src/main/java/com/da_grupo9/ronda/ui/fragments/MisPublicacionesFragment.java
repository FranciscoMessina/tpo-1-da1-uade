package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.ui.components.EmptyStateView;
import com.da_grupo9.ronda.ui.components.PublicationCardBinder;
import com.da_grupo9.ronda.util.MoneyFormat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class MisPublicacionesFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

    private LinearLayout publicacionesContainer;
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

    }

    @Override
    public void onResume() {
        super.onResume();
        if (publicacionesContainer != null) mostrarPublicaciones();
    }

    private void mostrarPublicaciones() {
        publicacionesContainer.removeAllViews();

        publicacionRepository.getMisPublicaciones(new RepositoryResult<List<Publicacion>>() {
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

    /**
     * Usa la misma tarjeta que Inicio y Favoritos; lo propio de esta pantalla son
     * el estado de la publicación y los botones de gestión.
     */
    private void agregarPublicacion(Publicacion publicacion) {
        View tarjeta = PublicationCardBinder.inflate(getLayoutInflater(), publicacionesContainer);
        PublicationCardBinder.bind(
                tarjeta,
                publicacion.getTitulo(),
                publicacion.getDescripcion(),
                MoneyFormat.amount(publicacion.getPrecio()),
                publicacion.getEstado(),
                publicacion.getCategoria(),
                publicacion.getZona(),
                null
        );
        PublicationCardBinder.setStatus(tarjeta, publicacion.getEstadoPublicacionVisible());
        PublicationCardBinder.favoriteButton(tarjeta).setVisibility(View.GONE);

        tarjeta.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("publicacionId", publicacion.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_misPublicacionesFragment_to_detailFragment, args);
        });

        agregarAccionesDeGestion(tarjeta, publicacion);

        publicacionesContainer.addView(tarjeta);
    }

    private void agregarAccionesDeGestion(View tarjeta, Publicacion publicacion) {
        String estadoPublicacion = publicacion.getEstadoPublicacion();

        if (!"sold".equals(estadoPublicacion)) {
            PublicationCardBinder.addAction(tarjeta, "Editar", R.drawable.ic_edit, v -> {
                Bundle args = new Bundle();
                args.putString("publicacionId", publicacion.getId());
                Navigation.findNavController(v).navigate(
                        R.id.action_misPublicacionesFragment_to_publicarArticuloFragment, args);
            });
        }

        if ("active".equals(estadoPublicacion)) {
            PublicationCardBinder.addAction(tarjeta, "Pausar", R.drawable.ic_pause, v ->
                    publicacionRepository.cambiarEstadoPublicacion(
                            publicacion.getId(), "paused", recargarAlFinalizar()));

        } else if ("paused".equals(estadoPublicacion)) {
            PublicationCardBinder.addAction(tarjeta, "Reactivar", R.drawable.ic_play_arrow, v ->
                    publicacionRepository.cambiarEstadoPublicacion(
                            publicacion.getId(), "active", recargarAlFinalizar()));
        }
    }

    private View crearMensajeVacio(String texto) {
        return EmptyStateView.create(getLayoutInflater(), publicacionesContainer, texto);
    }

    private RepositoryResult<Publicacion> recargarAlFinalizar() {
        return new RepositoryResult<Publicacion>() {
            @Override public void onSuccess(Publicacion data) { mostrarPublicaciones(); }
            @Override public void onError(String mensaje) {
                if (isAdded()) android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_LONG).show();
            }
        };
    }
}
