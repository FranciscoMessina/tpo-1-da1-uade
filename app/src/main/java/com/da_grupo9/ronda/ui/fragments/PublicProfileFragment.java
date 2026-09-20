package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.util.DateTimeFormat;
import com.da_grupo9.ronda.ui.components.EmptyStateView;
import com.da_grupo9.ronda.ui.components.PublicationCardBinder;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.model.ReviewItem;
import com.da_grupo9.ronda.data.model.ReviewsResponse;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.util.MoneyFormat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class PublicProfileFragment extends Fragment {

    private static final int PAGE_SIZE_RESENAS = 10;

    @Inject PublicacionRepository publicacionRepository;

    private String usuarioId = "";
    private int paginaResenas;
    private int totalPaginasResenas;
    private boolean cargandoResenas;
    private LinearLayout containerCalificaciones;
    private Button buttonMasCalificaciones;

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
        TextView textZona = view.findViewById(R.id.textPerfilPublicoZona);
        TextView textReputacion = view.findViewById(R.id.textPerfilPublicoReputacion);
        TextView textAntiguedad = view.findViewById(R.id.textPerfilPublicoAntiguedad);
        TextView textOperaciones = view.findViewById(R.id.textPerfilPublicoOperaciones);
        ImageView imageAvatar = view.findViewById(R.id.imagePerfilPublicoAvatar);

        LinearLayout containerPublicaciones =
                view.findViewById(R.id.containerPublicacionesVendedor);

        Button buttonVolver =
                view.findViewById(R.id.buttonVolverPerfilPublico);

        String vendedorNombre = "";
        String vendedorReputacion = "";

        if (getArguments() != null) {
            vendedorNombre = getArguments().getString("vendedorNombre", "");
            usuarioId = getArguments().getString("usuarioId", "");
            vendedorReputacion = getArguments().getString("vendedorReputacion", "");
        }

        textNombre.setText(vendedorNombre);
        textZona.setText("Cargando zona…");
        textReputacion.setText("Reputación: " + vendedorReputacion);

        textAntiguedad.setText("Cargando…");
        textOperaciones.setText("Cargando…");

        cargarPerfil(usuarioId, textNombre, textZona, textReputacion, textAntiguedad,
                textOperaciones, imageAvatar, containerPublicaciones);

        buttonVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        paginaResenas = 0;
        totalPaginasResenas = 1;
        cargandoResenas = false;
        containerCalificaciones = view.findViewById(R.id.containerCalificaciones);
        buttonMasCalificaciones = view.findViewById(R.id.buttonMasCalificaciones);
        buttonMasCalificaciones.setOnClickListener(v -> cargarResenas());
        cargarResenas();
    }

    private void cargarResenas() {
        if (cargandoResenas || usuarioId.isEmpty() || paginaResenas >= totalPaginasResenas) return;
        cargandoResenas = true;
        buttonMasCalificaciones.setEnabled(false);
        int pagina = paginaResenas + 1;
        publicacionRepository.getResenasUsuario(usuarioId, pagina, PAGE_SIZE_RESENAS,
                new RepositoryResult<ReviewsResponse>() {
            @Override public void onSuccess(ReviewsResponse respuesta) {
                if (!isAdded() || getView() == null) return;
                cargandoResenas = false;
                paginaResenas = pagina;
                PublicacionesResponse.Pagination paginacion = respuesta.getPagination();
                totalPaginasResenas = paginacion != null ? paginacion.getTotalPages() : pagina;
                renderizarResenas(respuesta.getItems());
                buttonMasCalificaciones.setEnabled(true);
                buttonMasCalificaciones.setVisibility(
                        paginaResenas < totalPaginasResenas ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String mensaje) {
                if (!isAdded() || getView() == null) return;
                cargandoResenas = false;
                buttonMasCalificaciones.setEnabled(true);
                if (pagina == 1) {
                    containerCalificaciones.addView(crearMensajeVacio(containerCalificaciones, mensaje));
                    buttonMasCalificaciones.setText("Reintentar");
                    buttonMasCalificaciones.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderizarResenas(List<ReviewItem> resenas) {
        // Al reintentar la primera página se descarta el mensaje de error previo.
        if (paginaResenas == 1) containerCalificaciones.removeAllViews();
        buttonMasCalificaciones.setText("Ver más calificaciones");

        if (resenas.isEmpty() && paginaResenas == 1) {
            containerCalificaciones.addView(crearMensajeVacio(containerCalificaciones, "El usuario todavía no recibió calificaciones."));
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ReviewItem resena : resenas) {
            View item = inflater.inflate(R.layout.item_review, containerCalificaciones, false);
            int rating = Math.max(0, Math.min(5, resena.getRating()));
            ((TextView) item.findViewById(R.id.textReviewEstrellas))
                    .setText("★".repeat(rating) + "☆".repeat(5 - rating));
            item.findViewById(R.id.textReviewEstrellas)
                    .setContentDescription(rating + " de 5 estrellas");

            TextView comentario = item.findViewById(R.id.textReviewComentario);
            String texto = resena.getComment();
            if (texto == null || texto.trim().isEmpty()) {
                comentario.setVisibility(View.GONE);
            } else {
                comentario.setText(texto);
            }

            ((TextView) item.findViewById(R.id.textReviewMeta)).setText(
                    resena.getReviewerName() + " · " + DateTimeFormat.mediumDate(resena.getCreatedAt()));
            containerCalificaciones.addView(item);
        }
    }

    private void cargarPerfil(String vendedorId, TextView nombre, TextView zona, TextView reputacion,
                              TextView antiguedad, TextView operaciones, ImageView avatar,
                              LinearLayout container) {
        publicacionRepository.getUsuario(vendedorId, new RepositoryResult<PublicUser>() {
            @Override public void onSuccess(PublicUser usuario) {
                if (!isAdded()) return;
                nombre.setText(usuario.getName());
                zona.setText(usuario.getZone() != null ? usuario.getZone() : "Zona no informada");
                reputacion.setText("Reputación: " + String.format(Locale.getDefault(), "%.1f (%d calificaciones)", usuario.getRatingAverage(), usuario.getRatingCount()));
                antiguedad.setText("Miembro desde: " + DateTimeFormat.mediumDate(usuario.getMemberSince()));
                operaciones.setText(usuario.getPurchasesCompleted() + " compras · "
                        + usuario.getSalesCompleted() + " ventas");
                if (usuario.getAvatarUrl() != null && !usuario.getAvatarUrl().isEmpty()) {
                    Glide.with(PublicProfileFragment.this).load(usuario.getAvatarUrl())
                            .placeholder(R.drawable.ic_photo_camera)
                            .error(R.drawable.ic_photo_camera).into(avatar);
                }
                renderizarPublicacionesActivas(usuario.getActivePublications(), container);
            }
            @Override public void onError(String mensaje) {
                if (!isAdded()) return;
                container.addView(crearMensajeVacio(container, mensaje));
            }
        });
    }

    /** Las publicaciones del vendedor usan la misma tarjeta que Inicio y Favoritos. */
    private void renderizarPublicacionesActivas(List<Publicacion> publicaciones, LinearLayout container) {
        int cantidad = 0;

        for (Publicacion publicacion : publicaciones) {
            View tarjeta = PublicationCardBinder.inflate(getLayoutInflater(), container);
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
            PublicationCardBinder.favoriteButton(tarjeta).setVisibility(View.GONE);

            tarjeta.setContentDescription("Ver detalle de " + publicacion.getTitulo());
            tarjeta.setOnClickListener(v -> {
                if (publicacion.getId() == null || publicacion.getId().isEmpty()) return;
                Bundle arguments = new Bundle();
                arguments.putString("publicacionId", publicacion.getId());
                Navigation.findNavController(v).navigate(
                        R.id.action_publicProfileFragment_to_detailFragment,
                        arguments
                );
            });

            container.addView(tarjeta);
            cantidad++;
        }

        if (cantidad == 0) {
            container.addView(crearMensajeVacio(container, "El usuario no tiene publicaciones activas."));
        }
    }

    private View crearMensajeVacio(LinearLayout container, String texto) {
        return EmptyStateView.create(getLayoutInflater(), container, texto);
    }
}
