package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.CreateReviewRequest;
import com.da_grupo9.ronda.data.model.Operation;
import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.remote.ProfileApi;
import com.da_grupo9.ronda.data.model.OperationsResponse;
import com.da_grupo9.ronda.util.ApiError;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ProfileRepository {
    private final ProfileApi api;
    @Inject public ProfileRepository(ProfileApi api) { this.api = api; }

    public void getMe(PublicacionRepository.Resultado<Perfil> result) { ejecutar(api.getMe(), result); }
    public void getOperations(PublicacionRepository.Resultado<OperationsResponse> result) {
        getOperations(null, null, null, result);
    }
    /** {@code type}, {@code from} y {@code to} son opcionales; from/to en ISO-8601. */
    public void getOperations(String type, String from, String to,
                              PublicacionRepository.Resultado<OperationsResponse> result) {
        ejecutarGenerico(api.getOperations(type, from, to), result);
    }
    public void getOperation(String operationId, PublicacionRepository.Resultado<Operation> result) {
        ejecutarGenerico(api.getOperation(operationId), result);
    }
    public void createReview(String operationId, int rating, String comment,
                             PublicacionRepository.Resultado<Void> result) {
        // La respuesta puede venir sin cuerpo, por eso alcanza con que sea exitosa.
        api.createReview(operationId, new CreateReviewRequest(rating, comment)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) result.onSuccess(null);
                else result.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
            }
            @Override public void onFailure(Call<Void> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }
    public void updateMe(Perfil perfil, PublicacionRepository.Resultado<Perfil> result) { ejecutar(api.updateMe(perfil), result); }
    private void ejecutar(Call<Perfil> call, PublicacionRepository.Resultado<Perfil> result) {
        call.enqueue(new Callback<Perfil>() {
            @Override public void onResponse(Call<Perfil> call, Response<Perfil> response) {
                if (response.isSuccessful() && response.body() != null) result.onSuccess(response.body());
                else result.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
            }
            @Override public void onFailure(Call<Perfil> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }

    private <T> void ejecutarGenerico(Call<T> call, PublicacionRepository.Resultado<T> result) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) result.onSuccess(response.body());
                else result.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }
}
