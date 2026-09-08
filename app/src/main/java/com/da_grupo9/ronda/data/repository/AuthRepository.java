package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.remote.AuthApi;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {
    public interface Resultado { void onSuccess(); void onError(String mensaje); }
    private final AuthApi api;

    @Inject public AuthRepository(AuthApi api) { this.api = api; }

    public void login(String email, String password, Resultado result) {
        Map<String, String> body = emailBody(email);
        body.put("password", password);
        ejecutar(api.login(body), result);
    }
    public void requestOtp(String email, Resultado result) { ejecutar(api.requestOtp(emailBody(email)), result); }
    public void resendOtp(String email, Resultado result) { ejecutar(api.resendOtp(emailBody(email)), result); }
    public void verifyOtp(String email, String code, Resultado result) {
        Map<String, String> body = emailBody(email);
        body.put("code", code);
        ejecutar(api.verifyOtp(body), result);
    }

    private Map<String, String> emailBody(String email) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        return body;
    }

    private void ejecutar(Call<ResponseBody> call, Resultado result) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) result.onSuccess();
                else result.onError("El servidor respondió con código " + response.code());
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
    }
}
