package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.ui.components.EmptyStateView;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.ui.PublicationAvailability;
import com.da_grupo9.ronda.util.NetworkMonitor;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.da_grupo9.ronda.data.model.FavoriteResponse;
import com.da_grupo9.ronda.data.repository.FavoritesRepository;
import com.da_grupo9.ronda.util.ApiError;


import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;



@AndroidEntryPoint
public class DetailFragment extends Fragment {

    @Inject
    FavoritesRepository favoritesRepository;
    @Inject PublicacionRepository publicacionRepository;
    private Publicacion publicacionCargada;

    private int fotoActualIndex = 0;
    private List<String> listaFotos;

    private String publicacionId = "";
    // Vistas
    private View bannerDetailOffline;
    private TextView textBannerDetailOffline;
    private View bannerPublicationUnavailable;
    private TextView textPublicationUnavailable;
    private boolean mostrandoCache;
    private ImageView imageFotoViewer;
    private TextView textFotoIndicador;
    private Button buttonFotoAnterior;
    private Button buttonFotoSiguiente;

    private TextView textDetailTitulo;
    private TextView textDetailPrecio;
    private TextView textDetailEstado;
    private TextView textDetailCategoria;
    private TextView textDetailZona;
    private TextView textDetailDireccion;
    private TextView textDetailFecha;
    private TextView textDetailDescripcion;

    private TextView textVendedorNombre;
    private TextView textVendedorReputacion;
    private Button buttonVerPerfilVendedor;

    private LinearLayout containerAccionesComprador;
    private View containerAccionesVendedor;
    private LinearLayout containerPreguntasComprador;
    private LinearLayout containerPreguntasRecibidas;

    private Button buttonPreguntar;
    private Button buttonOfertar;
    private Button buttonGuardar;

    private MaterialButton buttonModificar;
    private MaterialButton buttonPausar;

    private NetworkMonitor.NetworkStatusListener networkListener;
    private boolean previouslyOffline = false;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Vistas generales
        Button buttonBack = view.findViewById(R.id.buttonBack);
        bannerDetailOffline = view.findViewById(R.id.bannerDetailOffline);
        textBannerDetailOffline = view.findViewById(R.id.textBannerDetailOffline);
        bannerPublicationUnavailable = view.findViewById(R.id.bannerPublicationUnavailable);
        textPublicationUnavailable = view.findViewById(R.id.textPublicationUnavailable);

        // Galería
        imageFotoViewer = view.findViewById(R.id.imageFotoViewer);
        textFotoIndicador = view.findViewById(R.id.textFotoIndicador);
        buttonFotoAnterior = view.findViewById(R.id.buttonFotoAnterior);
        buttonFotoSiguiente = view.findViewById(R.id.buttonFotoSiguiente);

        // Detalle
        textDetailTitulo = view.findViewById(R.id.textDetailTitulo);
        textDetailPrecio = view.findViewById(R.id.textDetailPrecio);
        textDetailEstado = view.findViewById(R.id.textDetailEstado);
        textDetailCategoria = view.findViewById(R.id.textDetailCategoria);
        textDetailZona = view.findViewById(R.id.textDetailZona);
        textDetailDireccion = view.findViewById(R.id.textDetailDireccion);
        textDetailFecha = view.findViewById(R.id.textDetailFecha);
        textDetailDescripcion = view.findViewById(R.id.textDetailDescripcion);

        // Vendedor
        textVendedorNombre = view.findViewById(R.id.textVendedorNombre);
        textVendedorReputacion = view.findViewById(R.id.textVendedorReputacion);
        buttonVerPerfilVendedor = view.findViewById(R.id.buttonVerPerfilVendedor);

        // Contenedores
        containerAccionesComprador = view.findViewById(R.id.containerAccionesComprador);
        containerAccionesVendedor = view.findViewById(R.id.containerAccionesVendedor);
        containerPreguntasComprador = view.findViewById(R.id.containerPreguntasComprador);
        containerPreguntasRecibidas = view.findViewById(R.id.containerPreguntasRecibidas);

        // Botones
        buttonPreguntar = view.findViewById(R.id.buttonPreguntar);
        buttonOfertar = view.findViewById(R.id.buttonOfertar);
        buttonGuardar = view.findViewById(R.id.buttonGuardar);
        buttonModificar = view.findViewById(R.id.buttonModificar);
        buttonPausar = view.findViewById(R.id.buttonPausar);

        if (getArguments() != null) {
            publicacionId = getArguments().getString("publicacionId", "");
        }

        buttonBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        configurarListenersBotones();
        actualizarEstadoConexion(publicacionRepository.isOnline());

        networkListener = isOnline -> {
            if (!isAdded()) return;
            actualizarEstadoConexion(isOnline);
            if (isOnline && previouslyOffline) {
                previouslyOffline = false;
                Toast.makeText(requireContext(), "Conexión recuperada. Actualizando publicación...", Toast.LENGTH_SHORT).show();
                cargarDatosPublicacion(true);
            } else if (!isOnline) {
                previouslyOffline = true;
            }
        };
        publicacionRepository.getNetworkMonitor().addListener(networkListener);

        cargarDatosPublicacion(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkListener != null) {
            publicacionRepository.getNetworkMonitor().removeListener(networkListener);
        }
    }

    private void actualizarEstadoConexion(boolean isOnline) {
        if (bannerDetailOffline != null) {
            bannerDetailOffline.setVisibility(isOnline && !mostrandoCache ? View.GONE : View.VISIBLE);
        }
        if (textBannerDetailOffline != null) {
            textBannerDetailOffline.setText(isOnline
                    ? "No se pudo consultar el servidor: mostrando una copia guardada. La información podría no estar actualizada."
                    : "Modo sin conexión: mostrando una copia guardada. Las acciones están deshabilitadas.");
        }
        if (buttonPreguntar != null) {
            boolean puedePreguntar = publicacionCargada == null
                    || publicacionCargada.getActions() == null
                    || publicacionCargada.getActions().canAsk();
            buttonPreguntar.setEnabled(isOnline && puedePreguntar);
        }
        if (buttonOfertar != null) {
            boolean puedeOfertar = publicacionCargada == null
                    || publicacionCargada.getActions() == null
                    || publicacionCargada.getActions().canOffer();
            buttonOfertar.setEnabled(isOnline && puedeOfertar);
        }
        if (buttonGuardar != null) buttonGuardar.setEnabled(isOnline);
        if (buttonModificar != null) buttonModificar.setEnabled(isOnline);
        if (buttonPausar != null) buttonPausar.setEnabled(isOnline);
        if (buttonVerPerfilVendedor != null) buttonVerPerfilVendedor.setEnabled(isOnline);
    }

    private void cargarDatosPublicacion(boolean recargaSilenciosa) {
        if (publicacionId.isEmpty()) return;

        publicacionRepository.getPublicacionById(publicacionId, new RepositoryResult<Publicacion>() {
            @Override
            public void onSuccess(Publicacion data) {
                onSuccess(data, false);
            }

            @Override
            public void onSuccess(Publicacion data, boolean desdeCache) {
                if (!isAdded()) return;
                mostrandoCache = desdeCache;
                publicacionCargada = data;
                poblarDatos(data);
                actualizarEstadoConexion(publicacionRepository.isOnline());
            }

            @Override
            public void onError(String mensaje) {
                if (!isAdded()) return;
                if (!recargaSilenciosa) {
                    mostrarErrorDetalle(mensaje);
                }
            }

            @Override
            public void onError(ApiError error) {
                if (!isAdded()) return;
                // Los rechazos HTTP siempre se presentan, incluso durante la recarga automática:
                // la copia local ya no es válida para representar este detalle.
                mostrarErrorDetalle(error.getMessage());
            }
        });
    }

    private void mostrarErrorDetalle(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
        if (getView() != null) {
            Navigation.findNavController(getView()).popBackStack();
        }
    }

    private void poblarDatos(Publicacion publicacion) {
        textDetailTitulo.setText(publicacion.getTitulo());
        textDetailPrecio.setText("Precio: " + MoneyFormat.amount(publicacion.getPrecio()));

        boolean noDisponible = mostrarEstadoDisponibilidad(publicacion.getEstadoPublicacion());
        textDetailEstado.setText("Estado: " + publicacion.getEstado());

        textDetailCategoria.setText("Categoría: " + publicacion.getCategoria());
        textDetailZona.setText("Zona de entrega: " + publicacion.getZona());

        boolean mostrarDireccion =
                publicacion.getAddress() != null
                        && !publicacion.isAddressLocked();

        textDetailDireccion.setVisibility(
                mostrarDireccion ? View.VISIBLE : View.GONE
        );

        if (mostrarDireccion) {
            textDetailDireccion.setText(
                    "Dirección: " + publicacion.getAddress()
            );
        }

        textDetailFecha.setText(
                "Fecha de publicación: " + publicacion.getFechaPublicacion()
        );

        textDetailDescripcion.setText(publicacion.getDescripcion());

        textVendedorNombre.setText(
                "Vendedor: " + publicacion.getVendedorNombre()
        );

        textVendedorReputacion.setText(
                "Reputación: " + publicacion.getVendedorReputacion()
        );

        listaFotos = publicacion.getImagenes();
        fotoActualIndex = 0;
        actualizarGaleria();

        boolean esVendedor =
                publicacion.getActions() != null
                        && publicacion.getActions().canManage();

        if (esVendedor) {

            containerAccionesComprador.setVisibility(View.GONE);
            containerAccionesVendedor.setVisibility(View.VISIBLE);

            poblarPreguntasRecibidas(publicacion.getQuestions());

        } else {

            containerAccionesVendedor.setVisibility(View.GONE);

            if (noDisponible) {
                containerAccionesComprador.setVisibility(View.GONE);
            } else {
                containerAccionesComprador.setVisibility(View.VISIBLE);
            }

            poblarPreguntasComprador(publicacion.getQuestions());
        }

        actualizarBotonFavorito(publicacion);
        actualizarBotonPausar(buttonPausar, publicacion);
    }

    private boolean mostrarEstadoDisponibilidad(String status) {
        String label = PublicationAvailability.unavailableLabel(status);
        boolean noDisponible = label != null;
        bannerPublicationUnavailable.setVisibility(noDisponible ? View.VISIBLE : View.GONE);
        if (noDisponible) textPublicationUnavailable.setText(label);
        return noDisponible;
    }


    private void configurarListenersBotones() {
        buttonFotoAnterior.setOnClickListener(v -> {
            if (listaFotos != null && fotoActualIndex > 0) {
                fotoActualIndex--;
                actualizarGaleria();
            }
        });

        buttonFotoSiguiente.setOnClickListener(v -> {
            if (listaFotos != null && fotoActualIndex < listaFotos.size() - 1) {
                fotoActualIndex++;
                actualizarGaleria();
            }
        });

        buttonPreguntar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada != null) {
                if (publicacionCargada.getActions() != null
                        && !publicacionCargada.getActions().canAsk()) {
                    Toast.makeText(requireContext(), "No podés preguntar en esta publicación", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("publicacionId", publicacionCargada.getId());
                bundle.putString("publicacionTitulo", publicacionCargada.getTitulo());
                bundle.putString("vendedorNombre", publicacionCargada.getVendedorNombre());
                Navigation.findNavController(v).navigate(
                        R.id.action_detailFragment_to_questionFragment,
                        bundle
                );
            }
        });

        buttonOfertar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada != null) {
                if (publicacionCargada.getActions() != null
                        && !publicacionCargada.getActions().canOffer()) {
                    Toast.makeText(requireContext(), "No podés ofertar en esta publicación", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("publicacionId", publicacionCargada.getId());
                bundle.putString("publicacionTitulo", publicacionCargada.getTitulo());
                bundle.putFloat("publicacionPrecio", (float) publicacionCargada.getPrecio());
                bundle.putString("vendedorNombre", publicacionCargada.getVendedorNombre());
                Navigation.findNavController(v).navigate(
                        R.id.action_detailFragment_to_createOfferFragment,
                        bundle
                );
            }
        });

        buttonGuardar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(
                        requireContext(),
                        "Se necesita conexión a internet para continuar",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (publicacionCargada == null) {
                return;
            }

            boolean esFavorito = publicacionCargada.isFavorite();

            favoritesRepository.setFavorite(publicacionCargada.getId(), !esFavorito,
                    new RepositoryResult<FavoriteResponse>() {
                @Override
                public void onSuccess(FavoriteResponse data) {
                    if (!isAdded()) return;

                    Toast.makeText(
                            requireContext(),
                            esFavorito
                                    ? "Publicación quitada de favoritos"
                                    : "¡Publicación guardada en tus favoritos!",
                            Toast.LENGTH_SHORT
                    ).show();

                    cargarDatosPublicacion(true);
                }

                @Override
                public void onError(String mensaje) {
                    if (!isAdded()) return;

                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            });
        });

        buttonVerPerfilVendedor.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("vendedorNombre", publicacionCargada.getVendedorNombre());
            bundle.putString("usuarioId", publicacionCargada.getSeller() != null ? publicacionCargada.getSeller().getId() : "");
            bundle.putString("vendedorReputacion", publicacionCargada.getVendedorReputacion());

            Navigation.findNavController(v).navigate(R.id.action_detailFragment_to_publicProfileFragment, bundle);
        });

        buttonModificar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("publicacionId", publicacionCargada.getId());

            Navigation.findNavController(v).navigate(R.id.action_detailFragment_to_publicarArticuloFragment, bundle);
        });

        buttonPausar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada == null) return;
            boolean estaPausada = "paused".equalsIgnoreCase(publicacionCargada.getEstadoPublicacion());
            String nuevoEstado = estaPausada ? "active" : "paused";

            publicacionRepository.cambiarEstadoPublicacion(
                    publicacionCargada.getId(),
                    nuevoEstado,
                    new RepositoryResult<Publicacion>() {

                        @Override
                        public void onSuccess(Publicacion data) {
                            if (!isAdded()) return;

                            publicacionCargada = data;

                            // Actualiza todo el detalle con el nuevo estado
                            poblarDatos(data);

                            Toast.makeText(
                                    requireContext(),
                                    "La publicación ahora está "
                                            + data.getEstadoPublicacionVisible().toLowerCase(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onError(String mensaje) {
                            if (!isAdded()) return;

                            Toast.makeText(
                                    requireContext(),
                                    mensaje,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }

    private void actualizarBotonFavorito(Publicacion publicacion) {
        if (buttonGuardar == null || publicacion == null) return;

        if (publicacion.isFavorite()) {
            buttonGuardar.setText("Quitar de favoritos");
        } else {
            buttonGuardar.setText("Guardar");
        }
    }

    private void poblarPreguntasRecibidas(List<Publicacion.Question> preguntas) {
        poblarPreguntas(containerPreguntasRecibidas, preguntas,
                "Todavía no recibiste preguntas", true);
    }

    private void poblarPreguntasComprador(List<Publicacion.Question> preguntas) {
        poblarPreguntas(containerPreguntasComprador, preguntas,
                "Todavía no hay preguntas", false);
    }

    /**
     * Comprador y vendedor ven la misma tarjeta de pregunta; lo único que cambia
     * es si aparece el botón de responder.
     */
    private void poblarPreguntas(LinearLayout container, List<Publicacion.Question> preguntas,
                                 String mensajeVacio, boolean puedeResponder) {
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        if (preguntas.isEmpty()) {
            container.addView(EmptyStateView.create(inflater, container, mensajeVacio));
            return;
        }

        for (Publicacion.Question pregunta : preguntas) {
            View bloque = inflater.inflate(R.layout.item_question, container, false);

            ((TextView) bloque.findViewById(R.id.textQuestionText))
                    .setText("Pregunta: " + valorOBlanco(pregunta.getText()));

            TextView respuesta = bloque.findViewById(R.id.textQuestionAnswer);
            TextView respondida = bloque.findViewById(R.id.textQuestionAnsweredAt);
            MaterialButton responder = bloque.findViewById(R.id.buttonAnswerQuestion);

            boolean sinResponder = pregunta.getAnswer() == null;
            respuesta.setText(sinResponder
                    ? "Aún no fue respondida"
                    : "Respuesta: " + pregunta.getAnswer());

            boolean hayFecha = !sinResponder && pregunta.getAnsweredAt() != null;
            respondida.setVisibility(hayFecha ? View.VISIBLE : View.GONE);
            if (hayFecha) respondida.setText("Respondida: " + pregunta.getAnsweredAt());

            if (sinResponder && puedeResponder) {
                responder.setVisibility(View.VISIBLE);
                responder.setEnabled(publicacionRepository.isOnline());
                responder.setOnClickListener(v -> abrirDialogoRespuesta(pregunta));
            }

            container.addView(bloque);
        }
    }

    private void abrirDialogoRespuesta(Publicacion.Question pregunta) {
        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        int margen = dp(20);
        inputLayout.setPadding(margen, 0, margen, 0);
        inputLayout.setHint("Respuesta");
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(1000);

        TextInputEditText input = new TextInputEditText(inputLayout.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
        inputLayout.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Responder pregunta")
                .setMessage(pregunta.getText())
                .setView(inputLayout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> enviarRespuesta(dialog, inputLayout, input, pregunta)));
        dialog.show();
    }

    private void enviarRespuesta(AlertDialog dialog, TextInputLayout inputLayout,
                                 TextInputEditText input, Publicacion.Question pregunta) {
        String respuesta = input.getText() == null ? "" : input.getText().toString().trim();
        if (respuesta.isEmpty()) {
            inputLayout.setError("Escribí una respuesta antes de enviarla");
            return;
        }
        inputLayout.setError(null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        publicacionRepository.responderPregunta(pregunta.getId(), respuesta,
                new RepositoryResult<Publicacion.Question>() {
                    @Override public void onSuccess(Publicacion.Question data) {
                        if (!isAdded()) return;
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Respuesta enviada", Toast.LENGTH_SHORT).show();
                        cargarDatosPublicacion(true);
                    }

                    @Override public void onError(String mensaje) {
                        if (!isAdded()) return;
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String valorOBlanco(String valor) {
        return valor == null ? "" : valor;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
    private void actualizarBotonPausar(MaterialButton buttonPausar, Publicacion publicacion) {
        if (buttonPausar == null || publicacion == null) return;
        boolean estaPausada = "paused".equalsIgnoreCase(publicacion.getEstadoPublicacion());
        buttonPausar.setText(estaPausada ? "Reanudar publicación" : "Pausar publicación");
        buttonPausar.setIconResource(estaPausada ? R.drawable.ic_play_arrow : R.drawable.ic_pause);
    }

    private void actualizarGaleria() {
        if (imageFotoViewer == null || textFotoIndicador == null || buttonFotoAnterior == null || buttonFotoSiguiente == null) return;

        if (listaFotos == null || listaFotos.isEmpty()) {
            imageFotoViewer.setImageDrawable(null);
            textFotoIndicador.setText("Foto 0 de 0");
            buttonFotoAnterior.setEnabled(false);
            buttonFotoSiguiente.setEnabled(false);
            return;
        }

        String fotoItem = listaFotos.get(fotoActualIndex);
        Glide.with(this)
                .load(fotoItem)
                .centerCrop()
                .into(imageFotoViewer);

        textFotoIndicador.setText("Foto " + (fotoActualIndex + 1) + " de " + listaFotos.size());
        buttonFotoAnterior.setEnabled(fotoActualIndex > 0);
        buttonFotoSiguiente.setEnabled(fotoActualIndex < listaFotos.size() - 1);
    }
}
