package com.da_grupo9.ronda.ui.components;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.da_grupo9.ronda.R;

import java.util.Arrays;
import java.util.List;

/**
 * Adaptadores de Spinner con la tipografía y el espaciado de Ronda.
 *
 * <p>Sin esto cada pantalla usaría el layout por defecto de Android, que no
 * coincide con el de los campos de texto que tiene al lado.
 */
public final class SpinnerAdapters {
    private SpinnerAdapters() {}

    public static ArrayAdapter<String> create(Context context, List<String> items) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.item_spinner, items);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        return adapter;
    }

    public static ArrayAdapter<String> create(Context context, String[] items) {
        return create(context, Arrays.asList(items));
    }
}
