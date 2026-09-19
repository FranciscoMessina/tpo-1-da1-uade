package com.da_grupo9.ronda.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.da_grupo9.ronda.R;

/** Infla y enlaza los campos comunes de las tarjetas de publicaciones. */
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
        setText(card, R.id.textPublicationCondition, "Estado: " + condition, false);
        setText(card, R.id.textPublicationCategory,
                category == null ? null : "Categoría: " + category, true);
        setText(card, R.id.textPublicationZone, "Zona: " + zone, false);
        setText(card, R.id.textPublicationUpdate, update, true);
    }

    public static ImageButton favoriteButton(View card) {
        return card.findViewById(R.id.buttonPublicationFavorite);
    }

    private static void setText(View root, int id, String value, boolean hideWhenEmpty) {
        TextView view = root.findViewById(id);
        boolean empty = value == null || value.isEmpty();
        view.setVisibility(hideWhenEmpty && empty ? View.GONE : View.VISIBLE);
        if (!empty) view.setText(value);
    }
}
