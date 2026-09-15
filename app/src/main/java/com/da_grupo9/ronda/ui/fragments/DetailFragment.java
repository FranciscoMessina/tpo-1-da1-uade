package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.util.NetworkMonitor;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class DetailFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;
    private Publicacion publicacionCargada;

    private int fotoActualIndex = 0;
    private List<String> listaFotos;

    private String publicacionId = "";
    private String usuarioActualEmail = "";

    // Vistas
    private View bannerDetailOffline;
    private ImageView imageFotoViewer;
    private TextView textFotoIndicador;
    private Button buttonFotoAnterior;
    private Button buttonFotoSiguiente;

    private TextView textDetailTitulo;
    private TextView textDetailPrecio;
    private TextView textDetailEstado;
    private TextView textDetailCategoria;
    private TextView textDetailZona;
    private TextView textDetailFecha;
    private TextView textDetailDescripcion;

    private TextView textVendedorNombre;
    private TextView textVendedorReputacion;
    private Button buttonVerPerfilVendedor;

    private LinearLayout containerAccionesComprador;
    private LinearLayout containerAccionesVendedor;

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
        textDetailFecha = view.findViewById(R.id.textDetailFecha);
        textDetailDescripcion = view.findViewById(R.id.textDetailDescripcion);

        // Vendedor
        textVendedorNombre = view.findViewById(R.id.textVendedorNombre);
        textVendedorReputacion = view.findViewById(R.id.textVendedorReputacion);
        buttonVerPerfilVendedor = view.findViewById(R.id.buttonVerPerfilVendedor);

        // Contenedores
        containerAccionesComprador = view.findViewById(R.id.containerAccionesComprador);
        containerAccionesVendedor = view.findViewById(R.id.containerAccionesVendedor);

        // Botones
        buttonPreguntar = view.findViewById(R.id.buttonPreguntar);
        buttonOfertar = view.findViewById(R.id.buttonOfertar);
        buttonGuardar = view.findViewById(R.id.buttonGuardar);
        buttonModificar = view.findViewById(R.id.buttonModificar);
        buttonPausar = view.findViewById(R.id.buttonPausar);

        if (getArguments() != null) {
            publicacionId = getArguments().getString("publicacionId", "");
            usuarioActualEmail = getArguments().getString("usuarioActualEmail", "");
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
            bannerDetailOffline.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        }
        if (buttonPreguntar != null) buttonPreguntar.setEnabled(isOnline);
        if (buttonOfertar != null) buttonOfertar.setEnabled(isOnline);
        if (buttonGuardar != null) buttonGuardar.setEnabled(isOnline);
        if (buttonModificar != null) buttonModificar.setEnabled(isOnline);
        if (buttonPausar != null) buttonPausar.setEnabled(isOnline);
        if (buttonVerPerfilVendedor != null) buttonVerPerfilVendedor.setEnabled(isOnline);
    }

    private void cargarDatosPublicacion(boolean recargaSilenciosa) {
        if (publicacionId.isEmpty()) return;

        publicacionRepository.getPublicacionById(publicacionId, new PublicacionRepository.Resultado<Publicacion>() {
            @Override
            public void onSuccess(Publicacion data) {
                if (!isAdded()) return;
                publicacionCargada = data;
                poblarDatos(data);
                actualizarEstadoConexion(publicacionRepository.isOnline());
            }

            @Override
            public void onError(String mensaje) {
                if (!isAdded()) return;
                if (!recargaSilenciosa) {
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    if (publicacionCargada == null && getView() != null) {
                        Navigation.findNavController(getView()).popBackStack();
                    }
                }
            }
        });
    }

    private void poblarDatos(Publicacion publicacion) {
        textDetailTitulo.setText(publicacion.getTitulo());
        textDetailPrecio.setText("Precio: $" + String.format("%,.0f", publicacion.getPrecio()));
        textDetailEstado.setText("Estado: " + publicacion.getEstado());
        textDetailCategoria.setText("Categoría: " + publicacion.getCategoria());
        textDetailZona.setText("Zona de entrega: " + publicacion.getZona());
        textDetailFecha.setText("Fecha de publicación: " + publicacion.getFechaPublicacion());
        textDetailDescripcion.setText(publicacion.getDescripcion());

        textVendedorNombre.setText("Vendedor: " + publicacion.getVendedorNombre());
        textVendedorReputacion.setText("Reputación: " + publicacion.getVendedorReputacion());

        listaFotos = publicacion.getImagenes();
        fotoActualIndex = 0;
        actualizarGaleria();

        boolean esVendedor = publicacion.getActions() != null && publicacion.getActions().canManage();
        if (esVendedor) {
            containerAccionesComprador.setVisibility(View.GONE);
            containerAccionesVendedor.setVisibility(View.VISIBLE);
        } else {
            containerAccionesComprador.setVisibility(View.VISIBLE);
            containerAccionesVendedor.setVisibility(View.GONE);
        }

        actualizarBotonPausar(buttonPausar, publicacion);
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
                Toast.makeText(requireContext(), "Abriendo chat con " + publicacionCargada.getVendedorNombre(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonOfertar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada != null) {
                Toast.makeText(requireContext(), "Oferta de compra enviada a " + publicacionCargada.getVendedorNombre(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonGuardar.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), "¡Publicación guardada en tus favoritos!", Toast.LENGTH_SHORT).show();
        });

        buttonVerPerfilVendedor.setOnClickListener(v -> {
            if (!publicacionRepository.isOnline()) {
                Toast.makeText(requireContext(), "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            if (publicacionCargada == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("vendedorNombre", publicacionCargada.getVendedorNombre());
            bundle.putString("vendedorEmail", publicacionCargada.getSeller() != null ? publicacionCargada.getSeller().getId() : "");
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
            bundle.putString("email", usuarioActualEmail);
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
                    new PublicacionRepository.Resultado<Publicacion>() {
                        @Override public void onSuccess(Publicacion data) {
                            if (!isAdded()) return;
                            publicacionCargada = data;
                            actualizarBotonPausar(buttonPausar, data);
                            Toast.makeText(
                                    requireContext(),
                                    "La publicación ahora está " + data.getEstadoPublicacionVisible().toLowerCase(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        @Override public void onError(String mensaje) {
                            if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
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
