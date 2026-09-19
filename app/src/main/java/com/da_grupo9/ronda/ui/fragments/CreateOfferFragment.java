package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/** Punto de entrada al flujo de creación de una oferta. */
public class CreateOfferFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String titulo = args == null ? "" : args.getString("publicacionTitulo", "");
        float precio = args == null ? 0f : args.getFloat("precioPublicado", 0f);

        ((TextView) view.findViewById(R.id.textOfferPublication)).setText(titulo);
        ((TextView) view.findViewById(R.id.textOfferPublishedPrice))
                .setText("Precio publicado: $" + String.format("%,.0f", precio));
        TextInputEditText inputPrice = view.findViewById(R.id.inputOfferPrice);
        inputPrice.setText(formatearPrecioEditable(precio));
        view.findViewById(R.id.buttonOfferBack).setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack());
    }

    private String formatearPrecioEditable(float precio) {
        if (precio == Math.rint(precio)) {
            return String.format(Locale.US, "%.0f", precio);
        }
        return String.format(Locale.US, "%.2f", precio);
    }
}
