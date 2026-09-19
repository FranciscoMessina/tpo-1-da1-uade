package com.da_grupo9.ronda.ui.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.da_grupo9.ronda.R;

/**
 * Mensaje de lista vacía o de error dentro de un contenedor.
 *
 * <p>Existe para que todas las pantallas muestren el mismo texto centrado con la
 * misma tipografía y el mismo espaciado, en vez de construir un TextView a mano
 * en cada fragment.
 */
public final class EmptyStateView {
    private EmptyStateView() {}

    public static View create(LayoutInflater inflater, ViewGroup parent, String message) {
        TextView view = (TextView) inflater.inflate(R.layout.view_empty_state, parent, false);
        view.setText(message);
        return view;
    }
}
