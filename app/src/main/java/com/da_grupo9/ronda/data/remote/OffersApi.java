package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.model.CreateOfferRequest;
import com.da_grupo9.ronda.data.model.OfferActionResponse;
import com.da_grupo9.ronda.data.model.OffersResponse;
import com.da_grupo9.ronda.data.model.RespondCounterRequest;
import com.da_grupo9.ronda.data.model.RespondOfferRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OffersApi {
    @POST("publications/{id}/offers")
    Call<OfferActionResponse> createOffer(@Path("id") String publicationId,
                                          @Body CreateOfferRequest request);

    @POST("offers/{id}/respond")
    Call<OfferActionResponse> respond(@Path("id") String offerId,
                                      @Body RespondOfferRequest request);

    @POST("offers/{id}/respond-to-counter")
    Call<OfferActionResponse> respondToCounter(@Path("id") String offerId,
                                               @Body RespondCounterRequest request);

    @POST("offers/{id}/cancel")
    Call<OfferActionResponse> cancel(@Path("id") String offerId);

    @GET("me/offers")
    Call<OffersResponse> getMyOffers();
}
