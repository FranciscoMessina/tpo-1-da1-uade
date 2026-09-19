package com.da_grupo9.ronda.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.da_grupo9.ronda.R;
import com.google.android.material.button.MaterialButton;

/**
 * Infla y enlaza los campos comunes de las tarjetas de publicaciones.
 *
 * <p>Toda lista de publicaciones de la app (Inicio, Favoritos, Mis publicaciones y
 * el perfil público) pasa por acá, de modo que una publicación se ve igual en
 * cualquier pantalla.
 */
public final class PublicationCardBinder {
    private PublicationCardBinder() {}

    public static View inflate(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.item_publication, parent, false);
    }

    public static void bind(View card, String title, String description, String price,
                            String condition, String category, String zone, String update) {
        setText(card, R.id.textPublicationTitle, title, false);
        setText(card, R.id.textPublicationDescription, description, true);
        setText(card, R.id.textPublicationPrice, "Precio: " + price, false);
        setText(card, R.id.textPublicationCondition,
                condition == null ? null : "Estado: " + condition, true);
        setText(card, R.id.textPublicationCategory,
                category == null ? null : "Categoría: " + category, true);
        setText(card, R.id.textPublicationZone, zone == null ? null : "Zona: " + zone, true);
        setText(card, R.id.textPublicationUpdate, update, true);
    }

    public static ImageButton favoriteButton(View card) {
        return card.findViewById(R.id.buttonPublicationFavorite);
    }

    /** Estado de la publicación (activa, pausada, vendida). Solo lo usa el dueño. */
    public static void setStatus(View card, String status) {
        setText(card, R.id.textPublicationStatus,
                status == null ? null : "Estado de publicación: " + status, true);
    }

    /**
     * Agrega una acción del dueño (editar, pausar, reactivar) a la fila inferior.
     * Las acciones se reparten el ancho en partes iguales.
     */
    public static MaterialButton addAction(View card, String label, int iconRes,
                                           View.OnClickListener listener) {
        LinearLayout actions = card.findViewById(R.id.containerPublicationActions);
        actions.setVisibility(View.VISIBLE);

        MaterialButton button = new MaterialButton(actions.getContext(), null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(label);
        if (iconRes != 0) button.setIconResource(iconRes);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (actions.getChildCount() > 0) {
            params.setMarginStart(actions.getResources()
                    .getDimensionPixelSize(R.dimen.spacing_sm));
        }
        button.setLayoutParams(params);
        button.setOnClickListener(listener);

        actions.addView(button);
        return button;
    }

    private static void setText(View root, int id, String value, boolean hideWhenEmpty) {
        TextView view = root.findViewById(id);
        boolean empty = value == null || value.isEmpty();
        view.setVisibility(hideWhenEmpty && empty ? View.GONE : View.VISIBLE);
        if (!empty) view.setText(value);
    }
}
