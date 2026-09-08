package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@AndroidEntryPoint
public class PublicarArticuloFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

    private LinearLayout containerPaso1;
    private LinearLayout containerPaso2;
    private LinearLayout containerPaso3;

    private EditText editTitulo;
    private EditText editDescripcion;
    private EditText editPrecio;
    private EditText editZona;

    private Spinner spinnerCategoria;
    private Spinner spinnerEstado;

    private TextView textPaso;
    private TextView textResumen;

    private Button buttonAnterior;
    private Button buttonSiguiente;

    private int pasoActual = 1;
    private String email = "";

    public PublicarArticuloFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_publicar_articulo,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        containerPaso1 = view.findViewById(R.id.containerPaso1);
        containerPaso2 = view.findViewById(R.id.containerPaso2);
        containerPaso3 = view.findViewById(R.id.containerPaso3);

        editTitulo = view.findViewById(R.id.editTituloPublicacion);
        editDescripcion = view.findViewById(R.id.editDescripcionPublicacion);
        editPrecio = view.findViewById(R.id.editPrecioPublicacion);
        editZona = view.findViewById(R.id.editZonaPublicacion);

        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaPublicacion);
        spinnerEstado = view.findViewById(R.id.spinnerEstadoPublicacion);

        textPaso = view.findViewById(R.id.textPaso);
        textResumen = view.findViewById(R.id.textResumenPublicacion);

        buttonAnterior = view.findViewById(R.id.buttonAnteriorPaso);
        buttonSiguiente = view.findViewById(R.id.buttonSiguientePaso);
        Button buttonPublicar = view.findViewById(R.id.buttonPublicar);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        configurarSpinners();
        mostrarPaso();

        buttonAnterior.setOnClickListener(v -> {
            if (pasoActual > 1) {
                pasoActual--;
                mostrarPaso();
            }
        });

        buttonSiguiente.setOnClickListener(v -> avanzarPaso());
        buttonPublicar.setOnClickListener(v -> publicar(v));
    }

    private void configurarSpinners() {
        String[] categorias = {
                "Tecnología",
                "Deportes",
                "Ropa"
        };

        String[] estados = {
                "Nuevo",
                "Como nuevo",
                "Usado"
        };

        spinnerCategoria.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categorias
                )
        );

        spinnerEstado.setAdapter(
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        estados
                )
        );
    }

    private void avanzarPaso() {
        if (pasoActual == 1 && !validarPaso1()) {
            return;
        }

        if (pasoActual == 2 && !validarPaso2()) {
            return;
        }

        if (pasoActual < 3) {
            pasoActual++;
            mostrarPaso();
        }
    }

    private boolean validarPaso1() {
        if (editTitulo.getText().toString().trim().isEmpty()
                || editDescripcion.getText().toString().trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Completá el título y la descripción",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private boolean validarPaso2() {
        String precioTexto = editPrecio.getText().toString().trim();
        String zona = editZona.getText().toString().trim();

        if (precioTexto.isEmpty() || zona.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Completá el precio y la zona de entrega",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        try {
            double precio = Double.parseDouble(precioTexto);

            if (precio <= 0) {
                Toast.makeText(
                        requireContext(),
                        "El precio debe ser mayor a cero",
                        Toast.LENGTH_SHORT
                ).show();

                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(
                    requireContext(),
                    "Ingresá un precio válido",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private void mostrarPaso() {
        containerPaso1.setVisibility(pasoActual == 1 ? View.VISIBLE : View.GONE);
        containerPaso2.setVisibility(pasoActual == 2 ? View.VISIBLE : View.GONE);
        containerPaso3.setVisibility(pasoActual == 3 ? View.VISIBLE : View.GONE);

        textPaso.setText("Paso " + pasoActual + " de 3");
        buttonAnterior.setEnabled(pasoActual > 1);
        buttonSiguiente.setVisibility(pasoActual < 3 ? View.VISIBLE : View.GONE);

        if (pasoActual == 3) {
            mostrarResumen();
        }
    }

    private void mostrarResumen() {
        String resumen =
                "Título: " + editTitulo.getText().toString().trim()
                        + "\nDescripción: " + editDescripcion.getText().toString().trim()
                        + "\nCategoría: " + spinnerCategoria.getSelectedItem().toString()
                        + "\nPrecio: $" + editPrecio.getText().toString().trim()
                        + "\nEstado: " + spinnerEstado.getSelectedItem().toString()
                        + "\nZona: " + editZona.getText().toString().trim();

        textResumen.setText(resumen);
    }

    private void publicar(View view) {
        if (!validarPaso1() || !validarPaso2()) {
            return;
        }

        if (email.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "No se pudo identificar el email del usuario",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Publicacion publicacion = new Publicacion(
                0,
                editTitulo.getText().toString().trim(),
                editDescripcion.getText().toString().trim(),
                Double.parseDouble(editPrecio.getText().toString().trim()),
                spinnerEstado.getSelectedItem().toString(),
                spinnerCategoria.getSelectedItem().toString(),
                editZona.getText().toString().trim(),
                Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()),
                "Usuario",
                email.trim(),
                "Sin calificaciones",
                new ArrayList<>()
        );

        publicacionRepository.agregarPublicacion(publicacion, new PublicacionRepository.Resultado<Publicacion>() {
            @Override public void onSuccess(Publicacion data) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Publicación creada", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
            @Override public void onError(String mensaje) {
                if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}
