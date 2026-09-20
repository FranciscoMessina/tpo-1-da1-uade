package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.CreateOfferRequest;
import com.da_grupo9.ronda.data.model.Offer;
import com.da_grupo9.ronda.data.model.OfferActionResponse;
import com.da_grupo9.ronda.data.model.OffersResponse;
import com.da_grupo9.ronda.data.model.RespondCounterRequest;
import com.da_grupo9.ronda.data.model.RespondOfferRequest;
import com.da_grupo9.ronda.data.remote.OffersApi;
import com.da_grupo9.ronda.util.ApiError;
import com.da_grupo9.ronda.util.NetworkMonitor;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class OffersRepository {
    private final OffersApi api;
    private final NetworkMonitor networkMonitor;

    @Inject public OffersRepository(OffersApi api, NetworkMonitor networkMonitor) {
        this.api = api;
        this.networkMonitor = networkMonitor;
    }

    public boolean isOnline() { return networkMonitor.isOnline(); }

    public void getMyOffers(RepositoryResult<List<Offer>> resultado) {
        if (!requireConnection(resultado)) return;
        api.getMyOffers().enqueue(new Callback<OffersResponse>() {
            @Override public void onResponse(Call<OffersResponse> call, Response<OffersResponse> response) {
                if (response.isSuccessful() && response.body() != null) resultado.onSuccess(response.body().getItems());
                else resultado.onError(ApiError.from(response, "No se pudieron cargar las ofertas"));
            }
            @Override public void onFailure(Call<OffersResponse> call, Throwable error) { resultado.onError(networkError(error)); }
        });
    }

    public void createOffer(String publicationId, double amount, String message,
                            RepositoryResult<OfferActionResponse> resultado) {
        if (!requireConnection(resultado)) return;
        execute(api.createOffer(publicationId, new CreateOfferRequest(amount, message)), resultado);
    }

    public void respond(String offerId, String action, Double counterAmount,
                        RepositoryResult<OfferActionResponse> resultado) {
        if (!requireConnection(resultado)) return;
        execute(api.respond(offerId, new RespondOfferRequest(action, counterAmount)), resultado);
    }

    public void respondToCounter(String offerId, String action, RepositoryResult<OfferActionResponse> resultado) {
        if (!requireConnection(resultado)) return;
        execute(api.respondToCounter(offerId, new RespondCounterRequest(action)), resultado);
    }

    public void cancel(String offerId, RepositoryResult<OfferActionResponse> resultado) {
        if (!requireConnection(resultado)) return;
        execute(api.cancel(offerId), resultado);
    }

    private <T> boolean requireConnection(RepositoryResult<T> resultado) {
        if (networkMonitor.isOnline()) return true;
        resultado.onError("Se necesita conexión a internet para gestionar ofertas");
        return false;
    }

    private <T> void execute(Call<T> call, RepositoryResult<T> resultado) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) resultado.onSuccess(response.body());
                else resultado.onError(ApiError.from(response, "No se pudo realizar la acción"));
            }
            @Override public void onFailure(Call<T> call, Throwable error) { resultado.onError(networkError(error)); }
        });
    }

    private String networkError(Throwable error) {
        return error instanceof IOException ? "No se pudo conectar con el servidor"
                : "No se pudo procesar la respuesta del servidor";
    }
}
