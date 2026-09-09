package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.remote.ProfileApi;
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
    public void updateMe(Perfil perfil, PublicacionRepository.Resultado<Perfil> result) { ejecutar(api.updateMe(perfil), result); }

    private void ejecutar(Call<Perfil> call, PublicacionRepository.Resultado<Perfil> result) {
        call.enqueue(new Callback<Perfil>() {
            @Override public void onResponse(Call<Perfil> call, Response<Perfil> response) {
                if (response.isSuccessful() && response.body() != null) result.onSuccess(response.body());
                else result.onError("El servidor respondió con código " + response.code());
            }
            @Override public void onFailure(Call<Perfil> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }
}
