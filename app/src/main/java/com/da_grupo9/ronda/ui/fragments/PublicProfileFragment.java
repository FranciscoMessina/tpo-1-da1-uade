package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.model.PublicacionesResponse;
import com.da_grupo9.ronda.data.model.ReviewItem;
import com.da_grupo9.ronda.data.model.ReviewsResponse;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.google.android.material.color.MaterialColors;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
        TextView textOperaciones = view.findViewById(R.id.textPerfilPublicoOperaciones);
        ImageView imageAvatar = view.findViewById(R.id.imagePerfilPublicoAvatar);

        LinearLayout containerPublicaciones =
                view.findViewById(R.id.containerPublicacionesVendedor);

        Button buttonVolver =
                view.findViewById(R.id.buttonVolverPerfilPublico);

        String vendedorNombre = "";
        String vendedorId = "";
        String vendedorReputacion = "";

        if (getArguments() != null) {
            vendedorNombre = getArguments().getString("vendedorNombre", "");
            vendedorId = getArguments().getString("vendedorEmail", "");
            vendedorReputacion = getArguments().getString("vendedorReputacion", "");
        }

        textNombre.setText(vendedorNombre);
        textEmail.setText("Cargando zona…");
        textReputacion.setText("Reputación: " + vendedorReputacion);

        textAntiguedad.setText("Cargando…");
        textOperaciones.setText("Cargando…");

        cargarPerfil(vendedorId, textNombre, textEmail, textReputacion, textAntiguedad,
                textOperaciones, imageAvatar, containerPublicaciones);

        buttonVolver.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        usuarioId = vendedorId;
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
                new PublicacionRepository.Resultado<ReviewsResponse>() {
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
                    containerCalificaciones.addView(crearMensajeVacio(mensaje));
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
            containerCalificaciones.addView(crearMensajeVacio("El usuario todavía no recibió calificaciones."));
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
                    resena.getReviewerName() + " · " + formatearFecha(resena.getCreatedAt()));
            containerCalificaciones.addView(item);
        }
    }

    private void cargarPerfil(String vendedorId, TextView nombre, TextView zona, TextView reputacion,
                              TextView antiguedad, TextView operaciones, ImageView avatar,
                              LinearLayout container) {
        publicacionRepository.getUsuario(vendedorId, new PublicacionRepository.Resultado<PublicUser>() {
            @Override public void onSuccess(PublicUser usuario) {
                if (!isAdded()) return;
                nombre.setText(usuario.getName());
                zona.setText(usuario.getZone() != null ? usuario.getZone() : "Zona no informada");
                reputacion.setText("Reputación: " + String.format(Locale.getDefault(), "%.1f (%d calificaciones)", usuario.getRatingAverage(), usuario.getRatingCount()));
                antiguedad.setText("Miembro desde: " + formatearFecha(usuario.getMemberSince()));
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
                container.addView(crearMensajeVacio(mensaje));
            }
        });
    }

    private void renderizarPublicacionesActivas(List<Publicacion> publicaciones, LinearLayout container) {
        int cantidad = 0;

        int colorOnSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK);

        for (Publicacion publicacion : publicaciones) {

            {

                TextView publicacionView =
                        new TextView(requireContext());

                publicacionView.setText(
                        "• "
                                + publicacion.getTitulo()
                                + " - "
                                + MoneyFormat.amount(publicacion.getPrecio())
                );

                publicacionView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
                publicacionView.setTextColor(colorOnSurface);
                publicacionView.setPaddingRelative(0, dpToPx(6), 0, dpToPx(6));
                publicacionView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_chevron_right, 0);
                publicacionView.setCompoundDrawablePadding(dpToPx(8));
                publicacionView.setClickable(true);
                publicacionView.setFocusable(true);
                publicacionView.setContentDescription("Ver detalle de " + publicacion.getTitulo());

                TypedValue selectableBackground = new TypedValue();
                if (requireContext().getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackground, selectableBackground, true)) {
                    publicacionView.setBackgroundResource(selectableBackground.resourceId);
                }

                publicacionView.setOnClickListener(v -> {
                    if (publicacion.getId() == null || publicacion.getId().isEmpty()) return;
                    Bundle arguments = new Bundle();
                    arguments.putString("publicacionId", publicacion.getId());
                    Navigation.findNavController(v).navigate(
                            R.id.action_publicProfileFragment_to_detailFragment,
                            arguments
                    );
                });

                container.addView(publicacionView);

                cantidad++;
            }
        }

        if (cantidad == 0) {
            container.addView(crearMensajeVacio("El usuario no tiene publicaciones activas."));
        }
    }

    private TextView crearMensajeVacio(String texto) {
        TextView mensaje = new TextView(requireContext());

        mensaje.setText(texto);
        mensaje.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        mensaje.setTextColor(
                MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY)
        );
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));

        return mensaje;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return "sin datos";
        try {
            return OffsetDateTime.parse(fecha).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        } catch (RuntimeException ignored) {
            return fecha;
        }
    }
}
