package com.da_grupo9.ronda.ui.fragments;

import android.graphics.Color;
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
import com.da_grupo9.ronda.data.remote.FavoritesApi;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

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

        view.findViewById(R.id.buttonBackFavorites)
                .setOnClickListener(v ->
                        Navigation.findNavController(v).popBackStack()
                );

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

                    mostrarFavoritos(response.body().getItems());

                } else {

                    Toast.makeText(
                            requireContext(),
                            "No se pudieron cargar los favoritos",
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

        MaterialCardView tarjeta = new MaterialCardView(requireContext());

        tarjeta.setRadius(
                getResources().getDimension(R.dimen.corner_radius_card)
        );

        tarjeta.setCardElevation(
                getResources().getDimension(R.dimen.card_elevation)
        );

        tarjeta.setContentPadding(
                dpToPx(16),
                dpToPx(16),
                dpToPx(16),
                dpToPx(16)
        );

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, dpToPx(12));
        tarjeta.setLayoutParams(parametros);

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);

        tarjeta.addView(contenido);

        int colorOnSurface = MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface,
                Color.BLACK
        );

        int colorOnSurfaceVariant = MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                Color.DKGRAY
        );

        int colorPrice = androidx.core.content.ContextCompat.getColor(
                requireContext(),
                R.color.price
        );

        TextView titulo = new TextView(requireContext());
        titulo.setText(favorito.getTitle());
        titulo.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_TitleMedium
        );
        titulo.setTextColor(colorOnSurface);

        TextView precio = new TextView(requireContext());
        precio.setText(
                "Precio: $" + String.format("%,.0f", favorito.getPrice())
        );
        precio.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_TitleSmall
        );
        precio.setTextColor(colorPrice);
        precio.setPaddingRelative(0, dpToPx(8), 0, 0);

        TextView estado = new TextView(requireContext());
        estado.setText(
                "Estado: " + convertirCondicion(favorito.getItemCondition())
        );
        estado.setTextColor(colorOnSurfaceVariant);
        estado.setPaddingRelative(0, dpToPx(4), 0, 0);

        TextView zona = new TextView(requireContext());
        zona.setText("Zona: " + favorito.getZone());
        zona.setTextColor(colorOnSurfaceVariant);

        contenido.addView(titulo);
        contenido.addView(precio);
        contenido.addView(estado);
        contenido.addView(zona);

        if (favorito.hasPriceChanged()) {

            TextView novedad = new TextView(requireContext());
            novedad.setText("¡El precio cambió!");
            novedad.setTextAppearance(
                    com.google.android.material.R.style.TextAppearance_Material3_LabelLarge
            );
            novedad.setPaddingRelative(0, dpToPx(8), 0, 0);

            contenido.addView(novedad);
        }

        TextView verDetalle = new TextView(requireContext());
        verDetalle.setText("Ver detalle");
        verDetalle.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_LabelLarge
        );
        verDetalle.setPaddingRelative(0, dpToPx(12), 0, 0);

        contenido.addView(verDetalle);

        tarjeta.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("publicacionId", favorito.getId());

            Navigation.findNavController(v).navigate(
                    R.id.action_favoritesFragment_to_detailFragment,
                    bundle
            );
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

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }
}