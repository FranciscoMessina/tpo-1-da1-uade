package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;

@AndroidEntryPoint
public class DetailFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;
    private Publicacion publicacionCargada;

    private int fotoActualIndex = 0;
    private List<String> listaFotos;

    public DetailFragment() {
        // Constructor vacío obligatorio
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
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Vistas generales
        Button buttonBack = view.findViewById(R.id.buttonBack);

        // Vistas de la galería
        TextView textFotoViewer = view.findViewById(R.id.textFotoViewer);
        TextView textFotoIndicador = view.findViewById(R.id.textFotoIndicador);
        Button buttonFotoAnterior = view.findViewById(R.id.buttonFotoAnterior);
        Button buttonFotoSiguiente = view.findViewById(R.id.buttonFotoSiguiente);

        // Vistas de detalle del artículo
        TextView textDetailTitulo = view.findViewById(R.id.textDetailTitulo);
        TextView textDetailPrecio = view.findViewById(R.id.textDetailPrecio);
        TextView textDetailEstado = view.findViewById(R.id.textDetailEstado);
        TextView textDetailCategoria = view.findViewById(R.id.textDetailCategoria);
        TextView textDetailZona = view.findViewById(R.id.textDetailZona);
        TextView textDetailFecha = view.findViewById(R.id.textDetailFecha);
        TextView textDetailDescripcion = view.findViewById(R.id.textDetailDescripcion);

        // Vistas del vendedor
        TextView textVendedorNombre = view.findViewById(R.id.textVendedorNombre);
        TextView textVendedorReputacion = view.findViewById(R.id.textVendedorReputacion);
        Button buttonVerPerfilVendedor = view.findViewById(R.id.buttonVerPerfilVendedor);

        // Contenedores de acciones por rol
        LinearLayout containerAccionesComprador = view.findViewById(R.id.containerAccionesComprador);
        LinearLayout containerAccionesVendedor = view.findViewById(R.id.containerAccionesVendedor);

        // Botones de comprador
        Button buttonPreguntar = view.findViewById(R.id.buttonPreguntar);
        Button buttonOfertar = view.findViewById(R.id.buttonOfertar);
        Button buttonGuardar = view.findViewById(R.id.buttonGuardar);

        // Botones de vendedor
        Button buttonModificar = view.findViewById(R.id.buttonModificar);
        Button buttonPausar = view.findViewById(R.id.buttonPausar);

        // Obtener argumentos pasados por Navigation Component
        String publicacionId = "";

        if (getArguments() != null) {
            publicacionId = getArguments().getString("publicacionId", "");
        }

        if (publicacionCargada == null) {
            publicacionRepository.getPublicacionById(publicacionId, new PublicacionRepository.Resultado<Publicacion>() {
                @Override public void onSuccess(Publicacion data) {
                    if (!isAdded()) return;
                    publicacionCargada = data;
                    onViewCreated(view, savedInstanceState);
                }
                @Override public void onError(String mensaje) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    Navigation.findNavController(view).popBackStack();
                }
            });
            return;
        }
        Publicacion publicacion = publicacionCargada;

        // Cargar datos en la UI
        textDetailTitulo.setText(publicacion.getTitulo());
        textDetailPrecio.setText("Precio: $" + String.format("%,.0f", publicacion.getPrecio()));
        textDetailEstado.setText("Estado: " + publicacion.getEstado());
        textDetailCategoria.setText("Categoría: " + publicacion.getCategoria());
        textDetailZona.setText("Zona de entrega: " + publicacion.getZona());
        textDetailFecha.setText("Fecha de publicación: " + publicacion.getFechaPublicacion());
        textDetailDescripcion.setText(publicacion.getDescripcion());

        // Datos del vendedor
        textVendedorNombre.setText("Vendedor: " + publicacion.getVendedorNombre());
        textVendedorReputacion.setText("Reputación: " + publicacion.getVendedorReputacion());

        // Configurar galería de fotos
        listaFotos = publicacion.getImagenes();
        actualizarGaleria(textFotoViewer, textFotoIndicador, buttonFotoAnterior, buttonFotoSiguiente);

        buttonFotoAnterior.setOnClickListener(v -> {
            if (fotoActualIndex > 0) {
                fotoActualIndex--;
                actualizarGaleria(textFotoViewer, textFotoIndicador, buttonFotoAnterior, buttonFotoSiguiente);
            }
        });

        buttonFotoSiguiente.setOnClickListener(v -> {
            if (fotoActualIndex < listaFotos.size() - 1) {
                fotoActualIndex++;
                actualizarGaleria(textFotoViewer, textFotoIndicador, buttonFotoAnterior, buttonFotoSiguiente);
            }
        });

        // Determinar si el usuario actual es el vendedor o un comprador interesado
        boolean esVendedor = publicacion.getActions() != null && publicacion.getActions().canManage();

        if (esVendedor) {
            containerAccionesComprador.setVisibility(View.GONE);
            containerAccionesVendedor.setVisibility(View.VISIBLE);
        } else {
            containerAccionesComprador.setVisibility(View.VISIBLE);
            containerAccionesVendedor.setVisibility(View.GONE);
        }

        // Listeners para Comprador
        buttonPreguntar.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Abriendo chat con " + publicacion.getVendedorNombre(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        buttonOfertar.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Oferta de compra enviada a " + publicacion.getVendedorNombre(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        buttonGuardar.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "¡Publicación guardada en tus favoritos!",
                        Toast.LENGTH_SHORT
                ).show()
        );

        buttonVerPerfilVendedor.setOnClickListener(v -> {

            Bundle bundle = new Bundle();

            bundle.putString(
                    "vendedorNombre",
                    publicacion.getVendedorNombre()
            );

            bundle.putString(
                    "vendedorEmail",
                    publicacion.getSeller() != null ? publicacion.getSeller().getId() : ""
            );

            bundle.putString(
                    "vendedorReputacion",
                    publicacion.getVendedorReputacion()
            );

            Navigation.findNavController(v)
                    .navigate(
                            R.id.action_detailFragment_to_publicProfileFragment,
                            bundle
                    );
        });

        // Listeners para Vendedor
        buttonModificar.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Abriendo editor para: " + publicacion.getTitulo(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        buttonPausar.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "La publicación ha sido pausada",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Botón Volver con Navigation Component
        buttonBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void actualizarGaleria(
            TextView textFotoViewer,
            TextView textFotoIndicador,
            Button buttonFotoAnterior,
            Button buttonFotoSiguiente) {

        if (listaFotos == null || listaFotos.isEmpty()) {
            textFotoViewer.setText("📷 Sin fotos disponibles");
            textFotoIndicador.setText("Foto 0 de 0");
            buttonFotoAnterior.setEnabled(false);
            buttonFotoSiguiente.setEnabled(false);
            return;
        }

        textFotoViewer.setText(listaFotos.get(fotoActualIndex));
        textFotoIndicador.setText("Foto " + (fotoActualIndex + 1) + " de " + listaFotos.size());

        buttonFotoAnterior.setEnabled(fotoActualIndex > 0);
        buttonFotoSiguiente.setEnabled(fotoActualIndex < listaFotos.size() - 1);
    }
}
