package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.SavedSearchItem;
import com.da_grupo9.ronda.data.model.SavedSearchesResponse;
import com.da_grupo9.ronda.data.remote.SavedSearchesApi;
import com.da_grupo9.ronda.util.ApiErrorMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class SavedSearchesFragment extends Fragment {

    @Inject
    SavedSearchesApi savedSearchesApi;

    private LinearLayout busquedasContainer;
    private TextView textoSinBusquedas;

    public SavedSearchesFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_saved_searches,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        busquedasContainer =
                view.findViewById(R.id.busquedasContainer);

        textoSinBusquedas =
                view.findViewById(R.id.textoSinBusquedas);

        cargarBusquedas();
    }

    private void cargarBusquedas() {

        savedSearchesApi.getSavedSearches()
                .enqueue(new Callback<SavedSearchesResponse>() {

                    @Override
                    public void onResponse(
                            Call<SavedSearchesResponse> call,
                            Response<SavedSearchesResponse> response) {

                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            mostrarBusquedas(response.body());

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    ApiErrorMessage.from(response, "No se pudieron cargar las búsquedas"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<SavedSearchesResponse> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Error de conexión",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void mostrarBusquedas(
            SavedSearchesResponse response) {

        busquedasContainer.removeAllViews();

        if (response.getItems().isEmpty()) {
            textoSinBusquedas.setVisibility(View.VISIBLE);
            return;
        }

        textoSinBusquedas.setVisibility(View.GONE);

        for (SavedSearchItem busqueda : response.getItems()) {

            LinearLayout contenedorBusqueda =
                    new LinearLayout(requireContext());

            contenedorBusqueda.setOrientation(LinearLayout.VERTICAL);
            contenedorBusqueda.setPadding(16, 16, 16, 16);

            TextView textoBusqueda =
                    new TextView(requireContext());

            String texto =
                    busqueda.getName()
                            + "\nNovedades: "
                            + busqueda.getUnreadCount();

            textoBusqueda.setText(texto);
            textoBusqueda.setTextSize(18);

            Button botonUsarBusqueda =
                    new Button(requireContext());

            botonUsarBusqueda.setText("Usar búsqueda");

            botonUsarBusqueda.setOnClickListener(
                    v -> usarBusqueda(busqueda)
            );

            Button botonEliminar =
                    new Button(requireContext());

            botonEliminar.setText("Eliminar");

            botonEliminar.setOnClickListener(
                    v -> eliminarBusqueda(busqueda.getId())
            );

            contenedorBusqueda.addView(textoBusqueda);
            contenedorBusqueda.addView(botonUsarBusqueda);
            contenedorBusqueda.addView(botonEliminar);

            busquedasContainer.addView(contenedorBusqueda);
        }
    }

    private void usarBusqueda(SavedSearchItem busqueda) {

        savedSearchesApi.markAsRead(busqueda.getId())
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {

                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            abrirBusquedaEnHome(busqueda);
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    ApiErrorMessage.from(response, "No se pudo abrir la búsqueda"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Error de conexión",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void abrirBusquedaEnHome(SavedSearchItem busqueda) {

        Bundle bundle = new Bundle();

        bundle.putString("query", busqueda.getQuery());
        bundle.putString("category", busqueda.getCategory());
        bundle.putString("condition", busqueda.getCondition());
        bundle.putString("zone", busqueda.getZone());
        bundle.putString("sort", busqueda.getSort());

        if (busqueda.getMinPrice() != null) {
            bundle.putDouble("minPrice", busqueda.getMinPrice());
        }

        if (busqueda.getMaxPrice() != null) {
            bundle.putDouble("maxPrice", busqueda.getMaxPrice());
        }

        androidx.navigation.Navigation
                .findNavController(requireView())
                .navigate(
                        R.id.homeFragment,
                        bundle
                );
    }
    private void eliminarBusqueda(String id) {

        savedSearchesApi.deleteSavedSearch(id)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {

                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    requireContext(),
                                    "Búsqueda eliminada",
                                    Toast.LENGTH_SHORT
                            ).show();

                            cargarBusquedas();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    ApiErrorMessage.from(response, "No se pudo eliminar la búsqueda"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Error de conexión",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
