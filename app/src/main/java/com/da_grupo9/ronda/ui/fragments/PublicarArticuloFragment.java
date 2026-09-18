package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.local.BorradorPublicacionStorage;
import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AndroidEntryPoint
public class PublicarArticuloFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;
    @Inject SessionManager sessionManager;

    private LinearLayout containerPaso1;
    private LinearLayout containerPaso2;
    private LinearLayout containerPaso3;
    private LinearLayout containerCargaFotos;
    private LinearLayout containerUbicacionExacta;

    private EditText editTitulo;
    private EditText editDescripcion;
    private EditText editPrecio;
    private EditText editZona;
    private EditText editDireccion;
    private EditText editLatitud;
    private EditText editLongitud;

    private Spinner spinnerCategoria;
    private Spinner spinnerEstado;

    private TextView textPaso;
    private TextView textResumen;
    private TextView textCantidadFotos;
    private LinearLayout containerFotos;

    private Button buttonAnterior;
    private Button buttonSiguiente;

    private int pasoActual = 1;
    private String email = "";
    private String publicacionId;
    private BorradorPublicacionStorage borradorStorage;
    private final List<String> fotosSeleccionadas = new ArrayList<>();
    private final ExecutorService fileExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean publicacionCreada;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            copiarFotoAlAlmacenamientoInterno(uri);
                        }
                    }
            );

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
        containerCargaFotos = view.findViewById(R.id.containerCargaFotos);
        containerUbicacionExacta = view.findViewById(R.id.containerUbicacionExacta);

        editTitulo = view.findViewById(R.id.editTituloPublicacion);
        editDescripcion = view.findViewById(R.id.editDescripcionPublicacion);
        editPrecio = view.findViewById(R.id.editPrecioPublicacion);
        editZona = view.findViewById(R.id.editZonaPublicacion);
        editDireccion = view.findViewById(R.id.editDireccionPublicacion);
        editLatitud = view.findViewById(R.id.editLatitudPublicacion);
        editLongitud = view.findViewById(R.id.editLongitudPublicacion);

        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaPublicacion);
        spinnerEstado = view.findViewById(R.id.spinnerEstadoPublicacion);

        textPaso = view.findViewById(R.id.textPaso);
        textResumen = view.findViewById(R.id.textResumenPublicacion);
        textCantidadFotos = view.findViewById(R.id.textCantidadFotos);
        containerFotos = view.findViewById(R.id.containerFotosSeleccionadas);

        buttonAnterior = view.findViewById(R.id.buttonAnteriorPaso);
        buttonSiguiente = view.findViewById(R.id.buttonSiguientePaso);
        Button buttonPublicar = view.findViewById(R.id.buttonPublicar);
        Button buttonSeleccionarFoto = view.findViewById(R.id.buttonSeleccionarFoto);
        Button buttonQuitarFotos = view.findViewById(R.id.buttonQuitarFotos);
        Button buttonDescartarBorrador = view.findViewById(R.id.buttonDescartarBorrador);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            publicacionId = getArguments().getString("publicacionId", null);
        }

        String usuarioBorrador = email.trim().isEmpty()
                ? sessionManager.getUserId()
                : email;
        borradorStorage = new BorradorPublicacionStorage(requireContext(), usuarioBorrador);

        configurarSpinners();

        if (publicacionId != null && !publicacionId.isEmpty()) {
            cargarDatosParaEditar(publicacionId);
            buttonPublicar.setText("Guardar cambios");
            buttonDescartarBorrador.setVisibility(View.GONE);
            containerCargaFotos.setVisibility(View.GONE);
            containerUbicacionExacta.setVisibility(View.GONE);
        } else {
            restaurarBorrador();
        }

        mostrarPaso();

        buttonSeleccionarFoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        buttonQuitarFotos.setOnClickListener(v -> quitarFotosSeleccionadas());
        buttonDescartarBorrador.setOnClickListener(v -> descartarBorrador());

        buttonAnterior.setOnClickListener(v -> {
            if (pasoActual > 1) {
                pasoActual--;
                mostrarPaso();
                guardarBorrador();
            }
        });

        buttonSiguiente.setOnClickListener(v -> avanzarPaso());
        buttonPublicar.setOnClickListener(v -> publicar(v));
    }

    private void configurarSpinners() {
        String[] categorias = {
                "Tecnología",
                "Hogar",
                "Deportes",
                "Ropa y moda",
                "Vehículos",
                "Libros",
                "Juguetes",
                "Otros"
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
            guardarBorrador();
        }
    }

    private boolean validarPaso1() {
        String titulo = editTitulo.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Completá el título y la descripción",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (titulo.length() < 3 || titulo.length() > 120) {
            Toast.makeText(
                    requireContext(),
                    "El título debe tener entre 3 y 120 caracteres",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (descripcion.length() < 10 || descripcion.length() > 5000) {
            Toast.makeText(
                    requireContext(),
                    "La descripción debe tener entre 10 y 5000 caracteres",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if ((publicacionId == null || publicacionId.isEmpty()) && fotosSeleccionadas.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Seleccioná al menos una foto",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    private boolean validarPaso2() {
        String precioTexto = editPrecio.getText().toString().trim();
        String zona = editZona.getText().toString().trim();
        String direccion = editDireccion.getText().toString().trim();
        String latitudTexto = editLatitud.getText().toString().trim();
        String longitudTexto = editLongitud.getText().toString().trim();

        boolean esEdicion = publicacionId != null && !publicacionId.isEmpty();

        if (precioTexto.isEmpty() || zona.isEmpty()
                || (!esEdicion && (direccion.isEmpty()
                || latitudTexto.isEmpty() || longitudTexto.isEmpty()))) {
            Toast.makeText(
                    requireContext(),
                    "Completá el precio y los datos de entrega",
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

        if (!esEdicion) {
            try {
                double latitud = Double.parseDouble(latitudTexto);
                double longitud = Double.parseDouble(longitudTexto);

                if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
                    Toast.makeText(
                            requireContext(),
                            "Ingresá coordenadas válidas",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(
                        requireContext(),
                        "Ingresá coordenadas válidas",
                        Toast.LENGTH_SHORT
                ).show();
                return false;
            }
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

        if (publicacionId == null || publicacionId.isEmpty()) {
            resumen += "\nDirección: " + editDireccion.getText().toString().trim()
                    + "\nCoordenadas: " + editLatitud.getText().toString().trim()
                    + ", " + editLongitud.getText().toString().trim()
                    + "\nFotos seleccionadas: " + fotosSeleccionadas.size();
        }

        textResumen.setText(resumen);
    }

    private void cargarDatosParaEditar(String id) {
        publicacionRepository.getPublicacionById(id, new PublicacionRepository.Resultado<Publicacion>() {
            @Override public void onSuccess(Publicacion p) {
                if (!isAdded()) return;

                editTitulo.setText(p.getTitulo());
                editDescripcion.setText(p.getDescripcion());
                editPrecio.setText(String.format(Locale.US, "%.0f", p.getPrecio()));
                editZona.setText(p.getZona());

                for (int i = 0; i < spinnerCategoria.getCount(); i++) {
                    if (spinnerCategoria.getItemAtPosition(i).toString().equalsIgnoreCase(p.getCategoria())) {
                        spinnerCategoria.setSelection(i);
                        break;
                    }
                }

                for (int i = 0; i < spinnerEstado.getCount(); i++) {
                    if (spinnerEstado.getItemAtPosition(i).toString().equalsIgnoreCase(p.getEstado())) {
                        spinnerEstado.setSelection(i);
                        break;
                    }
                }
            }
            @Override public void onError(String mensaje) {
                if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void publicar(View view) {
        if (!validarPaso1() || !validarPaso2()) {
            return;
        }

        Publicacion publicacion = new Publicacion(
                editTitulo.getText().toString().trim(),
                editDescripcion.getText().toString().trim(),
                Double.parseDouble(editPrecio.getText().toString().trim()),
                conditionApiValue(spinnerEstado.getSelectedItemPosition()),
                categoryApiValue(spinnerCategoria.getSelectedItemPosition()),
                editZona.getText().toString().trim(),
                7
        );

        if (publicacionId != null && !publicacionId.isEmpty()) {
            publicacionRepository.actualizarPublicacion(publicacionId, publicacion, new PublicacionRepository.Resultado<Publicacion>() {
                @Override public void onSuccess(Publicacion data) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Publicación modificada con éxito", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                }
                @Override public void onError(String mensaje) {
                    if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        publicacionRepository.agregarPublicacion(publicacion, new PublicacionRepository.Resultado<Publicacion>() {
            @Override public void onSuccess(Publicacion data) {
                if (!isAdded()) return;
                publicacionCreada = true;
                borradorStorage.borrar();
                Toast.makeText(requireContext(), "Publicación creada", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
            @Override public void onError(String mensaje) {
                if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String conditionApiValue(int position) {
        return new String[]{"new", "like_new", "used"}[position];
    }

    private String categoryApiValue(int position) {
        return new String[]{"electronics", "home", "sports", "fashion", "vehicles", "books", "toys", "other"}[position];
    }

    private void copiarFotoAlAlmacenamientoInterno(Uri uri) {
        android.content.Context context = requireContext().getApplicationContext();
        fileExecutor.execute(() -> {
            File directorio = new File(context.getFilesDir(), "borradores_publicacion");
            if (!directorio.exists() && !directorio.mkdirs()) {
                mostrarErrorFoto();
                return;
            }

            File destino = new File(directorio, "foto_" + System.currentTimeMillis() + ".img");

            try (InputStream input = context.getContentResolver().openInputStream(uri);
                 FileOutputStream output = new FileOutputStream(destino)) {
                if (input == null) {
                    mostrarErrorFoto();
                    return;
                }

                byte[] buffer = new byte[8192];
                int cantidad;
                while ((cantidad = input.read(buffer)) != -1) {
                    output.write(buffer, 0, cantidad);
                }

                mainHandler.post(() -> {
                    if (!isAdded()) {
                        destino.delete();
                        return;
                    }
                    fotosSeleccionadas.add(destino.getAbsolutePath());
                    mostrarFotosSeleccionadas();
                    guardarBorrador();
                });
            } catch (IOException e) {
                destino.delete();
                mostrarErrorFoto();
            }
        });
    }

    private void mostrarErrorFoto() {
        mainHandler.post(() -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarFotosSeleccionadas() {
        containerFotos.removeAllViews();
        textCantidadFotos.setText("Fotos seleccionadas: " + fotosSeleccionadas.size());

        int size = (int) (96 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (String ruta : fotosSeleccionadas) {
            ImageView imageView = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginEnd(margin);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            containerFotos.addView(imageView);

            Glide.with(this)
                    .load(new File(ruta))
                    .into(imageView);
        }
    }

    private void quitarFotosSeleccionadas() {
        for (String ruta : fotosSeleccionadas) {
            new File(ruta).delete();
        }
        fotosSeleccionadas.clear();
        mostrarFotosSeleccionadas();
        guardarBorrador();
    }

    private void guardarBorrador() {
        if (publicacionCreada || (publicacionId != null && !publicacionId.isEmpty())) {
            return;
        }

        boolean vacio = editTitulo.getText().toString().trim().isEmpty()
                && editDescripcion.getText().toString().trim().isEmpty()
                && editPrecio.getText().toString().trim().isEmpty()
                && editZona.getText().toString().trim().isEmpty()
                && editDireccion.getText().toString().trim().isEmpty()
                && editLatitud.getText().toString().trim().isEmpty()
                && editLongitud.getText().toString().trim().isEmpty()
                && fotosSeleccionadas.isEmpty()
                && pasoActual == 1;

        if (vacio) {
            borradorStorage.borrar();
            return;
        }

        BorradorPublicacionStorage.Borrador borrador = new BorradorPublicacionStorage.Borrador();
        borrador.titulo = editTitulo.getText().toString();
        borrador.descripcion = editDescripcion.getText().toString();
        borrador.precio = editPrecio.getText().toString();
        borrador.zona = editZona.getText().toString();
        borrador.direccion = editDireccion.getText().toString();
        borrador.latitud = editLatitud.getText().toString();
        borrador.longitud = editLongitud.getText().toString();
        borrador.categoria = spinnerCategoria.getSelectedItemPosition();
        borrador.estado = spinnerEstado.getSelectedItemPosition();
        borrador.paso = pasoActual;
        borrador.fotos.addAll(fotosSeleccionadas);
        borradorStorage.guardar(borrador);
    }

    private void restaurarBorrador() {
        BorradorPublicacionStorage.Borrador borrador = borradorStorage.cargar();
        if (borrador == null) {
            return;
        }

        editTitulo.setText(borrador.titulo);
        editDescripcion.setText(borrador.descripcion);
        editPrecio.setText(borrador.precio);
        editZona.setText(borrador.zona);
        editDireccion.setText(borrador.direccion);
        editLatitud.setText(borrador.latitud);
        editLongitud.setText(borrador.longitud);
        spinnerCategoria.setSelection(borrador.categoria);
        spinnerEstado.setSelection(borrador.estado);
        pasoActual = Math.max(1, Math.min(3, borrador.paso));
        fotosSeleccionadas.addAll(borrador.fotos);
        mostrarFotosSeleccionadas();

        Toast.makeText(requireContext(), "Borrador recuperado", Toast.LENGTH_SHORT).show();
    }

    private void descartarBorrador() {
        borradorStorage.borrar();
        fotosSeleccionadas.clear();
        editTitulo.setText("");
        editDescripcion.setText("");
        editPrecio.setText("");
        editZona.setText("");
        editDireccion.setText("");
        editLatitud.setText("");
        editLongitud.setText("");
        spinnerCategoria.setSelection(0);
        spinnerEstado.setSelection(0);
        pasoActual = 1;
        mostrarFotosSeleccionadas();
        mostrarPaso();
        Toast.makeText(requireContext(), "Borrador descartado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        guardarBorrador();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fileExecutor.shutdown();
    }
}
