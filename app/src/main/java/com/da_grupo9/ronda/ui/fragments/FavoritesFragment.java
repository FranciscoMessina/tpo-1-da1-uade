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
import com.da_grupo9.ronda.data.repository.FavoritesRepository;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.da_grupo9.ronda.ui.components.PublicationCardBinder;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {

    @Inject
    FavoritesRepository favoritesRepository;

    private LinearLayout favoritesContainer;
    private TextView textFavoritesEmpty;

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

        favoritesRepository.getFavorites(new RepositoryResult<FavoritesResponse>() {

            @Override
            public void onSuccess(FavoritesResponse favorites) {

                if (!isAdded()) return;

                mostrarFavoritos(favorites.getItems());
                if (favorites.getUnreadCount() > 0) {
                    marcarFavoritosComoLeidos();
                }
            }

            @Override
            public void onError(String mensaje) {

                if (!isAdded()) return;

                Toast.makeText(
                        requireContext(),
                        mensaje,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void marcarFavoritosComoLeidos() {
        favoritesRepository.markAsRead(new RepositoryResult<FavoritesReadResponse>() {
            @Override
            public void onSuccess(FavoritesReadResponse data) {
            }

            @Override
            public void onError(String mensaje) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
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
            bundle.putString("publicacionId", favorito.getPublicationId());
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
