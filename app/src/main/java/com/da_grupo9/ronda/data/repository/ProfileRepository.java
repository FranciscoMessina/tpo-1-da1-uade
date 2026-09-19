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

    public void getMe(RepositoryResult<Perfil> result) { ejecutar(api.getMe(), false, result); }
    public void getOperations(RepositoryResult<OperationsResponse> result) {
        getOperations(null, null, null, result);
    }
    /** {@code type}, {@code from} y {@code to} son opcionales; from/to en ISO-8601. */
    public void getOperations(String type, String from, String to,
                              RepositoryResult<OperationsResponse> result) {
        ejecutar(api.getOperations(type, from, to), false, result);
    }
    public void getOperation(String operationId, RepositoryResult<Operation> result) {
        ejecutar(api.getOperation(operationId), false, result);
    }
    public void createReview(String operationId, int rating, String comment,
                             RepositoryResult<Void> result) {
        ejecutar(api.createReview(operationId, new CreateReviewRequest(rating, comment)), true, result);
    }
    public void updateMe(Perfil perfil, RepositoryResult<Perfil> result) {
        ejecutar(api.updateMe(perfil), false, result);
    }

    private <T> void ejecutar(Call<T> call, boolean permiteCuerpoVacio, RepositoryResult<T> result) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && (permiteCuerpoVacio || response.body() != null)) {
                    result.onSuccess(response.body());
                }
                else result.onError(ApiError.from(response, "El servidor respondió con código " + response.code()));
            }
            @Override public void onFailure(Call<T> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }
}
