package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.FavoriteItem;
import com.da_grupo9.ronda.data.model.FavoritesResponse;
import com.da_grupo9.ronda.data.model.FavoritesReadResponse;
import com.da_grupo9.ronda.data.remote.FavoritesApi;
import com.da_grupo9.ronda.util.ApiErrorMessage;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.da_grupo9.ronda.ui.components.PublicationCardBinder;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {

    @Inject
    FavoritesApi favoritesApi;

    private LinearLayout favoritesContainer;
    private TextView textFavoritesEmpty;

    public FavoritesFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_favorites,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        favoritesContainer = view.findViewById(R.id.favoritesContainer);
        textFavoritesEmpty = view.findViewById(R.id.textFavoritesEmpty);

        cargarFavoritos();
    }

    private void cargarFavoritos() {

        favoritesApi.getFavorites().enqueue(new Callback<>() {

            @Override
            public void onResponse(
                    @NonNull Call<FavoritesResponse> call,
                    @NonNull Response<FavoritesResponse> response) {

                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {

                    FavoritesResponse favorites = response.body();
                    mostrarFavoritos(favorites.getItems());
                    if (favorites.getUnreadCount() > 0) {
                        marcarFavoritosComoLeidos();
                    }

                } else {

                    Toast.makeText(
                            requireContext(),
                            ApiErrorMessage.from(response, "No se pudieron cargar los favoritos"),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<FavoritesResponse> call,
                    @NonNull Throwable t) {

                if (!isAdded()) return;

                Toast.makeText(
                        requireContext(),
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void marcarFavoritosComoLeidos() {
        favoritesApi.markFavoritesAsRead().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FavoritesReadResponse> call,
                                   @NonNull Response<FavoritesReadResponse> response) {
                if (!response.isSuccessful() && isAdded()) {
                    Toast.makeText(requireContext(),
                            ApiErrorMessage.from(response, "No se pudieron marcar los favoritos como leídos"),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FavoritesReadResponse> call,
                                  @NonNull Throwable error) {
                // No se ocultan las novedades ni se interrumpe la pantalla si falla el acuse de lectura.
            }
        });
    }

    private void mostrarFavoritos(List<FavoriteItem> favoritos) {

        favoritesContainer.removeAllViews();

        if (favoritos.isEmpty()) {
            textFavoritesEmpty.setVisibility(View.VISIBLE);
            return;
        }

        textFavoritesEmpty.setVisibility(View.GONE);

        for (FavoriteItem favorito : favoritos) {
            agregarFavorito(favorito);
        }
    }

    private void agregarFavorito(FavoriteItem favorito) {
        View tarjeta = PublicationCardBinder.inflate(getLayoutInflater(), favoritesContainer);
        PublicationCardBinder.bind(
                tarjeta,
                favorito.getTitle(),
                null,
                MoneyFormat.amount(favorito.getPrice()),
                convertirCondicion(favorito.getItemCondition()),
                null,
                favorito.getZone(),
                favorito.hasPriceChanged() ? "¡El precio cambió!" : null
        );
        PublicationCardBinder.favoriteButton(tarjeta).setVisibility(View.GONE);

        tarjeta.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("publicacionId", favorito.getId());
            Navigation.findNavController(v).navigate(R.id.detailFragment, bundle);
        });

        favoritesContainer.addView(tarjeta);
    }

    private String convertirCondicion(String condicion) {

        if (condicion == null) return "Sin especificar";

        switch (condicion) {
            case "new":
                return "Nuevo";
            case "like_new":
                return "Como nuevo";
            case "used":
                return "Usado";
            default:
                return condicion;
        }
    }

}
