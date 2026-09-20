package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.FiltrosPublicaciones;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.google.android.material.color.MaterialColors;
import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.repository.ProfileRepository;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.widget.Toast;

import com.da_grupo9.ronda.data.model.FavoriteResponse;
import com.da_grupo9.ronda.data.model.SavedSearchRequest;
import com.da_grupo9.ronda.data.model.SavedSearchItem;
import com.da_grupo9.ronda.data.remote.FavoritesApi;
import com.da_grupo9.ronda.data.remote.SavedSearchesApi;
import com.da_grupo9.ronda.util.ApiErrorMessage;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.da_grupo9.ronda.ui.components.EmptyStateView;
import com.da_grupo9.ronda.ui.components.SpinnerAdapters;
import com.da_grupo9.ronda.ui.components.PublicationCardBinder;

import android.widget.ImageButton;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;
    @Inject SavedSearchesApi savedSearchesApi;
    @Inject FavoritesApi favoritesApi;
    @Inject ProfileRepository profileRepository;
    private LinearLayout publicacionesContainer;


    /** Peticiones de favorito en curso: id de publicación -> estado que se está intentando aplicar. */
    private final Map<String, Boolean> favoritosPendientes = new HashMap<>();
    /** Botones de favorito de las tarjetas visibles, por id de publicación. */
    private final Map<String, ImageButton> botonesFavorito = new HashMap<>();

    private EditText buscador;
    private EditText precioMinimo;
    private EditText precioMaximo;

    private Spinner spinnerCategoria;
    private Spinner spinnerEstado;
    private Spinner spinnerCercania;
    private Spinner spinnerOrden;

    private Button botonFiltrar;
    private Button botonGuardarBusqueda;
    private Button botonAnterior;
    private Button botonSiguiente;

    private TextView textoPagina;
    private TextView textoResultados;
    private View bannerOffline;
    private TextView textBannerOffline;
    private boolean mostrandoCache;
    private com.da_grupo9.ronda.util.NetworkMonitor.NetworkStatusListener networkListener;
    private boolean previouslyOffline = false;

    private List<Publicacion> publicacionesFiltradas;
    private FiltrosPublicaciones filtrosAplicados;

    private int paginaActual = 1;
    private final int publicacionesPorPagina = 3;
    private int totalPaginas = 1;
    private int totalResultados = 0;
    private final List<String> categoryCodes = new ArrayList<>();
    private final List<String> zoneValues = new ArrayList<>();
    private String zonaBusquedaGuardada;
    private String zonaUsuario;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        bannerOffline = view.findViewById(R.id.bannerOffline);
        textBannerOffline = view.findViewById(R.id.textBannerOffline);
        actualizarEstadoConexion(publicacionRepository.isOnline());

        networkListener = isOnline -> {
            if (!isAdded()) return;
            actualizarEstadoConexion(isOnline);
            if (isOnline && previouslyOffline) {
                previouslyOffline = false;
                android.widget.Toast.makeText(requireContext(), "Conexión recuperada. Actualizando publicaciones...", android.widget.Toast.LENGTH_SHORT).show();
                cargarDatos();
            } else if (!isOnline) {
                previouslyOffline = true;
            }
        };
        publicacionRepository.getNetworkMonitor().addListener(networkListener);

        buscador = view.findViewById(R.id.buscador);
        precioMinimo = view.findViewById(R.id.precioMinimo);
        precioMaximo = view.findViewById(R.id.precioMaximo);

        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        spinnerCercania = view.findViewById(R.id.spinnerCercania);
        spinnerOrden = view.findViewById(R.id.spinnerOrden);

        botonFiltrar = view.findViewById(R.id.botonFiltrar);
        botonAnterior = view.findViewById(R.id.botonAnterior);
        botonSiguiente = view.findViewById(R.id.botonSiguiente);

        textoPagina = view.findViewById(R.id.textoPagina);
        textoResultados = view.findViewById(R.id.textoResultados);

        botonGuardarBusqueda = view.findViewById(R.id.botonGuardarBusqueda);

        botonGuardarBusqueda.setOnClickListener(
                v -> mostrarDialogoGuardarBusqueda()
        );

        publicacionesContainer =
                view.findViewById(R.id.publicacionesContainer);

        configurarSpinners();
        cargarCategorias();
        cargarZonas();
        cargarZonaUsuario();
        cargarBusquedaGuardada();
        configurarVisibilidadGuardarBusqueda();

        publicacionesFiltradas = new ArrayList<>();
        filtrosAplicados = filtrosIniciales();
        cargarDatos();

        botonFiltrar.setOnClickListener(
                v -> aplicarFiltros()
        );

        botonAnterior.setOnClickListener(
                v -> paginaAnterior()
        );

        botonSiguiente.setOnClickListener(
                v -> paginaSiguiente()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkListener != null) {
            publicacionRepository.getNetworkMonitor().removeListener(networkListener);
        }
        botonesFavorito.clear();
    }

    private void actualizarEstadoConexion(boolean isOnline) {
        if (bannerOffline != null) {
            bannerOffline.setVisibility(isOnline && !mostrandoCache ? View.GONE : View.VISIBLE);
        }
        if (textBannerOffline != null) {
            textBannerOffline.setText(isOnline
                    ? "No se pudo consultar el servidor: mostrando publicaciones guardadas. La información podría no estar actualizada."
                    : "Modo sin conexión: mostrando publicaciones guardadas. La información podría no estar actualizada.");
        }
        for (String id : botonesFavorito.keySet()) {
            actualizarBotonFavorito(id);
        }
    }

    private void cargarDatos() {
        actualizarEstadoConexion(publicacionRepository.isOnline());

        publicacionRepository.getPublicaciones(paginaActual, publicacionesPorPagina, filtrosAplicados,
                new PublicacionRepository.ResultadoPagina() {

                    @Override
                    public void onSuccess(List<Publicacion> data, int page, int pages, int total) {
                        onSuccess(data, page, pages, total, false);
                    }

                    @Override
                    public void onSuccess(List<Publicacion> data, int page, int pages, int total,
                                          boolean desdeCache) {
                        if (!isAdded()) return;

                        mostrandoCache = desdeCache;
                        actualizarEstadoConexion(publicacionRepository.isOnline());
                        publicacionesFiltradas = new ArrayList<>(data);
                        paginaActual = page;
                        totalPaginas = Math.max(1, pages);
                        totalResultados = total;
                        mostrarPagina();
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (isAdded()) {
                            android.widget.Toast.makeText(
                                    requireContext(),
                                    mensaje,
                                    android.widget.Toast.LENGTH_LONG
                            ).show();

                            if (!publicacionRepository.isOnline()) {
                                actualizarEstadoConexion(false);
                            }
                        }
                    }
                });
    }

    private void configurarSpinners() {
        actualizarCategorias(java.util.Arrays.asList(
                "electronics", "home", "fashion", "sports", "vehicles", "books", "toys", "other"));

        String[] estados = {
                "Todos",
                "Nuevo",
                "Como nuevo",
                "Usado"
        };

        actualizarZonas(Collections.emptyList());

        String[] ordenamientos = {
                "Más recientes",
                "Menor precio",
                "Mayor precio"
        };

        spinnerEstado.setAdapter(
                SpinnerAdapters.create(requireContext(), estados)
        );

        spinnerOrden.setAdapter(
                SpinnerAdapters.create(requireContext(), ordenamientos)
        );
    }

    private void cargarCategorias() {
        publicacionRepository.getCategories(new RepositoryResult<List<String>>() {
            @Override public void onSuccess(List<String> data) {
                if (!isAdded() || data.isEmpty()) return;
                String selected = categoriaSeleccionadaApi();
                actualizarCategorias(data);
                seleccionarCategoriaGuardada(selected);
            }

            @Override public void onError(String mensaje) {
                // Se conserva el catálogo conocido para que Home siga disponible offline.
            }
        });
    }

    private void actualizarCategorias(List<String> codes) {
        categoryCodes.clear();
        categoryCodes.addAll(codes);
        List<String> labels = new ArrayList<>();
        labels.add("Todas");
        for (String code : codes) labels.add(etiquetaCategoria(code));
        spinnerCategoria.setAdapter(SpinnerAdapters.create(requireContext(), labels));
    }

    private void cargarZonaUsuario() {

        profileRepository.getMe(new RepositoryResult<Perfil>() {

            @Override
            public void onSuccess(Perfil perfil) {
                if (!isAdded()) {
                    return;
                }

                zonaUsuario = perfil.getZona();
            }

            @Override
            public void onError(String mensaje) {
                if (!isAdded()) {
                    return;
                }

                zonaUsuario = null;
            }
        });
    }
    private void cargarZonas() {
        publicacionRepository.getZones(new RepositoryResult<List<String>>() {
            @Override public void onSuccess(List<String> data) {
                if (!isAdded()) return;
                String selected = zonaSeleccionadaApi();
                actualizarZonas(data);
                seleccionarCercaniaGuardada(selected != null ? selected : zonaBusquedaGuardada);
            }

            @Override public void onError(String mensaje) {
                // Se mantiene "Todas las zonas" si no se puede cargar el catálogo.
            }
        });
    }

    private void actualizarZonas(List<String> zones) {
        zoneValues.clear();
        zoneValues.addAll(zones);

        List<String> labels = new ArrayList<>();
        labels.add("Todas las zonas");
        labels.add("Cerca de mí");
        labels.addAll(zones);

        spinnerCercania.setAdapter(
                SpinnerAdapters.create(requireContext(), labels)
        );
    }

    private void configurarVisibilidadGuardarBusqueda() {
        TextWatcher observadorTexto = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarVisibilidadGuardarBusqueda();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        buscador.addTextChangedListener(observadorTexto);
        precioMinimo.addTextChangedListener(observadorTexto);
        precioMaximo.addTextChangedListener(observadorTexto);

        AdapterView.OnItemSelectedListener observadorSeleccion =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        actualizarVisibilidadGuardarBusqueda();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        actualizarVisibilidadGuardarBusqueda();
                    }
                };

        spinnerCategoria.setOnItemSelectedListener(observadorSeleccion);
        spinnerEstado.setOnItemSelectedListener(observadorSeleccion);
        spinnerCercania.setOnItemSelectedListener(observadorSeleccion);
        spinnerOrden.setOnItemSelectedListener(observadorSeleccion);

        actualizarVisibilidadGuardarBusqueda();
    }

    private void actualizarVisibilidadGuardarBusqueda() {
        boolean hayValoresNoPredeterminados =
                !buscador.getText().toString().trim().isEmpty()
                        || !precioMinimo.getText().toString().trim().isEmpty()
                        || !precioMaximo.getText().toString().trim().isEmpty()
                        || spinnerCategoria.getSelectedItemPosition() != 0
                        || spinnerEstado.getSelectedItemPosition() != 0
                        || spinnerCercania.getSelectedItemPosition() != 0
                        || spinnerOrden.getSelectedItemPosition() != 0;

        botonGuardarBusqueda.setVisibility(
                hayValoresNoPredeterminados ? View.VISIBLE : View.GONE
        );
    }

    private void mostrarDialogoGuardarBusqueda() {

        EditText inputNombre = new EditText(requireContext());
        inputNombre.setHint("Ej: Notebooks baratas");

        new AlertDialog.Builder(requireContext())
                .setTitle("Guardar búsqueda")
                .setMessage("Ingresá un nombre para identificar esta búsqueda")
                .setView(inputNombre)
                .setPositiveButton("Guardar", (dialog, which) -> {

                    String nombre =
                            inputNombre.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "Ingresá un nombre para la búsqueda",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    guardarBusqueda(nombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarBusqueda(String nombre) {

        final FiltrosPublicaciones filtros;
        try {
            filtros = leerFiltrosEnEdicion();
        } catch (IllegalArgumentException error) {
            Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        SavedSearchRequest request =
                new SavedSearchRequest(
                        nombre,
                        filtros.getQuery(),
                        filtros.getCategory(),
                        filtros.getMinPrice(),
                        filtros.getMaxPrice(),
                        filtros.getCondition(),
                        filtros.getZone(),
                        filtros.getSort()
                );

        savedSearchesApi.createSavedSearch(request)
                .enqueue(new Callback<SavedSearchItem>() {

                    @Override
                    public void onResponse(
                            Call<SavedSearchItem> call,
                            Response<SavedSearchItem> response) {

                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Búsqueda guardada correctamente",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    ApiErrorMessage.from(response, "Error al guardar: " + response.code()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<SavedSearchItem> call,
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

    private void cargarBusquedaGuardada() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }
        String query = args.getString("query");
        String category = args.getString("category");
        String condition = args.getString("condition");
        String zone = args.getString("zone");
        String sort = args.getString("sort");

        if (query != null) {
            buscador.setText(query);
        }

        if (args.containsKey("minPrice")) {
            precioMinimo.setText(
                    String.valueOf(args.getDouble("minPrice"))
            );
        }

        if (args.containsKey("maxPrice")) {
            precioMaximo.setText(
                    String.valueOf(args.getDouble("maxPrice"))
            );
        }

        seleccionarCategoriaGuardada(category);
        seleccionarEstadoGuardado(condition);
        seleccionarCercaniaGuardada(zone);
        seleccionarOrdenGuardado(sort);
    }

    private void seleccionarCategoriaGuardada(String category) {

        if (category == null) {
            spinnerCategoria.setSelection(0);
            return;
        }
        int index = categoryCodes.indexOf(category);
        spinnerCategoria.setSelection(index >= 0 ? index + 1 : 0);
    }

    private void seleccionarEstadoGuardado(String condition) {

        if (condition == null) {
            spinnerEstado.setSelection(0);
            return;
        }

        switch (condition) {
            case "new":
                spinnerEstado.setSelection(1);
                break;
            case "like_new":
                spinnerEstado.setSelection(2);
                break;
            case "used":
                spinnerEstado.setSelection(3);
                break;
            default:
                spinnerEstado.setSelection(0);
                break;
        }
    }

    private void seleccionarCercaniaGuardada(String zone) {

        if (zone == null) {
            zonaBusquedaGuardada = null;
            spinnerCercania.setSelection(0);
            return;
        }

        zonaBusquedaGuardada = zone;

        // Si la búsqueda guardada usa la misma zona del usuario mostramos "Cerca de mí".
        if (zonaUsuario != null && zone.equalsIgnoreCase(zonaUsuario)) {
            spinnerCercania.setSelection(1);
            return;
        }

        int index = zoneValues.indexOf(zone);

        // +2 porque:
        // posición 0 = "Todas las zonas"
        // posición 1 = "Cerca de mí"
        // posición 2 en adelante = zonas reales
        spinnerCercania.setSelection(index >= 0 ? index + 2 : 0);
    }

    private void seleccionarOrdenGuardado(String sort) {

        if (sort == null) {
            spinnerOrden.setSelection(0);
            return;
        }

        switch (sort) {
            case "recent":
                spinnerOrden.setSelection(0);
                break;
            case "price_asc":
                spinnerOrden.setSelection(1);
                break;
            case "price_desc":
                spinnerOrden.setSelection(2);
                break;
            default:
                spinnerOrden.setSelection(0);
                break;
        }
    }
    private void aplicarFiltros() {
        try {
            filtrosAplicados = leerFiltrosEnEdicion();
            paginaActual = 1;
            cargarDatos();
        } catch (IllegalArgumentException error) {
            Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarPagina() {
        if (publicacionesFiltradas.isEmpty()) {

            mostrarPublicaciones(
                    publicacionesFiltradas
            );

            paginaActual = 1;

            textoPagina.setText(
                    "Página 1 de 1"
            );
            textoResultados.setText("0 resultados");

            botonAnterior.setEnabled(false);
            botonSiguiente.setEnabled(false);

            return;
        }

        mostrarPublicaciones(publicacionesFiltradas);

        textoPagina.setText(
                "Página "
                        + paginaActual
                        + " de "
                        + totalPaginas
        );
        textoResultados.setText(totalResultados + (totalResultados == 1 ? " resultado" : " resultados"));

        botonAnterior.setEnabled(
                paginaActual > 1
        );

        botonSiguiente.setEnabled(
                paginaActual < totalPaginas
        );
    }

    private void paginaAnterior() {

        if (paginaActual > 1) {

            paginaActual--;
            cargarDatos();
        }
    }

    private void paginaSiguiente() {
        if (paginaActual < totalPaginas) {

            paginaActual++;
            cargarDatos();
        }
    }

    private void mostrarPublicaciones(
            List<Publicacion> lista) {

        publicacionesContainer.removeAllViews();
        botonesFavorito.clear();

        if (lista.isEmpty()) {

            publicacionesContainer.addView(
                    crearMensajeVacio("No se encontraron publicaciones.")
            );

            return;
        }

        for (Publicacion publicacion : lista) {

            agregarPublicacion(publicacion);
        }
    }

    private void agregarPublicacion(Publicacion publicacion) {
        View tarjeta = PublicationCardBinder.inflate(getLayoutInflater(), publicacionesContainer);
        PublicationCardBinder.bind(
                tarjeta,
                publicacion.getTitulo(),
                publicacion.getDescripcion(),
                MoneyFormat.amount(publicacion.getPrecio()),
                publicacion.getEstado(),
                publicacion.getCategoria(),
                publicacion.getZona(),
                null
        );

        ImageButton botonFavorito = PublicationCardBinder.favoriteButton(tarjeta);
        if (publicacion.getActions() == null || publicacion.getActions().canFavorite()) {
            configurarBotonFavorito(publicacion, botonFavorito);
        } else {
            botonFavorito.setVisibility(View.GONE);
        }

        tarjeta.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("publicacionId", publicacion.getId());
            Navigation.findNavController(v).navigate(
                    R.id.action_homeFragment_to_detailFragment, bundle);
        });

        publicacionesContainer.addView(tarjeta);
    }

    private void configurarBotonFavorito(Publicacion publicacion, ImageButton boton) {
        boton.setOnClickListener(v -> alternarFavorito(publicacion));
        botonesFavorito.put(publicacion.getId(), boton);
        actualizarBotonFavorito(publicacion.getId());
    }

    private Publicacion buscarPublicacion(String id) {
        if (publicacionesFiltradas == null) return null;
        for (Publicacion p : publicacionesFiltradas) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    private boolean esFavorito(Publicacion publicacion) {
        Boolean pendiente = favoritosPendientes.get(publicacion.getId());
        return pendiente != null ? pendiente : publicacion.isFavorite();
    }

    private void actualizarBotonFavorito(String id) {
        ImageButton boton = botonesFavorito.get(id);
        Publicacion publicacion = buscarPublicacion(id);
        if (boton == null || publicacion == null) return;

        boolean favorito = esFavorito(publicacion);
        boton.setImageResource(favorito ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        boton.setImageTintList(android.content.res.ColorStateList.valueOf(
                favorito
                        ? MaterialColors.getColor(boton, android.R.attr.colorPrimary, Color.BLUE)
                        : MaterialColors.getColor(boton, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY)));
        boton.setContentDescription(favorito ? "Quitar de favoritos" : "Guardar en favoritos");

        boolean habilitado = publicacionRepository.isOnline() && !favoritosPendientes.containsKey(id);
        boton.setEnabled(habilitado);
        boton.setAlpha(habilitado ? 1f : 0.4f);
    }

    private void alternarFavorito(Publicacion publicacion) {
        String id = publicacion.getId();
        if (id == null || favoritosPendientes.containsKey(id)) return;

        if (!publicacionRepository.isOnline()) {
            Toast.makeText(requireContext(),
                    "Se necesita conexión a internet para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nuevoEstado = !publicacion.isFavorite();
        favoritosPendientes.put(id, nuevoEstado);
        actualizarBotonFavorito(id);

        Call<FavoriteResponse> llamada = nuevoEstado
                ? favoritesApi.addFavorite(id)
                : favoritesApi.removeFavorite(id);

        llamada.enqueue(new Callback<FavoriteResponse>() {
            @Override
            public void onResponse(Call<FavoriteResponse> call, Response<FavoriteResponse> response) {
                if (response.isSuccessful()) {
                    resolverFavorito(id, nuevoEstado, null);
                } else {
                    resolverFavorito(id, !nuevoEstado, ApiErrorMessage.from(response, nuevoEstado
                            ? "No se pudo guardar la publicación"
                            : "No se pudo quitar de favoritos"));
                }
            }

            @Override
            public void onFailure(Call<FavoriteResponse> call, Throwable t) {
                resolverFavorito(id, !nuevoEstado, "Error de conexión");
            }
        });
    }

    /** Cierra la petición en curso dejando {@code estadoFinal} y avisa si hubo error. */
    private void resolverFavorito(String id, boolean estadoFinal, String error) {
        favoritosPendientes.remove(id);
        // La lista pudo recargarse mientras la petición estaba en vuelo, por eso se busca por id.
        Publicacion actual = buscarPublicacion(id);
        if (actual != null) actual.setFavorite(estadoFinal);
        if (!isAdded()) return;
        actualizarBotonFavorito(id);
        if (error != null) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    private View crearMensajeVacio(String texto) {
        return EmptyStateView.create(getLayoutInflater(), publicacionesContainer, texto);
    }

    private FiltrosPublicaciones leerFiltrosEnEdicion() {
        return FiltrosPublicaciones.crear(buscador.getText().toString(), categoriaSeleccionadaApi(),
                condicionSeleccionadaApi(), zonaSeleccionadaApi(), precioMinimo.getText().toString(),
                precioMaximo.getText().toString(), ordenSeleccionadoApi());
    }

    private FiltrosPublicaciones filtrosIniciales() {
        Bundle args = getArguments();
        if (args == null) return leerFiltrosEnEdicion();
        String min = args.containsKey("minPrice") ? String.valueOf(args.getDouble("minPrice")) : null;
        String max = args.containsKey("maxPrice") ? String.valueOf(args.getDouble("maxPrice")) : null;
        return FiltrosPublicaciones.crear(args.getString("query"), args.getString("category"),
                args.getString("condition"), args.getString("zone"), min, max, args.getString("sort"));
    }

    private String categoriaSeleccionadaApi() {
        int position = spinnerCategoria.getSelectedItemPosition();
        return position > 0 && position <= categoryCodes.size() ? categoryCodes.get(position - 1) : null;
    }

    private String condicionSeleccionadaApi() {
        switch (spinnerEstado.getSelectedItemPosition()) {
            case 1: return "new";
            case 2: return "like_new";
            case 3: return "used";
            default: return null;
        }
    }

    private String zonaSeleccionadaApi() {
        int position = spinnerCercania.getSelectedItemPosition();

        if (position == 0) {
            return null;
        }

        if (position == 1) {
            if (zonaUsuario == null || zonaUsuario.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Configurá tu zona en el perfil para usar Cerca de mí"
                );
            }

            return zonaUsuario;
        }

        int zoneIndex = position - 2;

        return zoneIndex >= 0 && zoneIndex < zoneValues.size()
                ? zoneValues.get(zoneIndex)
                : null;
    }

    private String ordenSeleccionadoApi() {
        switch (spinnerOrden.getSelectedItemPosition()) {
            case 1: return "price_asc";
            case 2: return "price_desc";
            default: return "recent";
        }
    }

    private String etiquetaCategoria(String code) {
        switch (code) {
            case "electronics": return "Tecnología";
            case "home": return "Hogar";
            case "fashion": return "Ropa y moda";
            case "sports": return "Deportes";
            case "vehicles": return "Vehículos";
            case "books": return "Libros";
            case "toys": return "Juguetes";
            case "other": return "Otros";
            default: return code;
        }
    }

}
