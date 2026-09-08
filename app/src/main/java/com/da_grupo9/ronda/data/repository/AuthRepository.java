package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.model.LoginResponse;
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
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(AuthApi api, SessionManager sessionManager) {
        this.api = api;
        this.sessionManager = sessionManager;
    }

    public void login(String email, String password, Resultado result) {
        Map<String, String> body = emailBody(email);
        body.put("password", password);
        ejecutarConSesion(api.login(body), result);
    }
    public void requestOtp(String email, Resultado result) { ejecutar(api.requestOtp(emailBody(email)), result); }
    public void resendOtp(String email, Resultado result) { ejecutar(api.resendOtp(emailBody(email)), result); }
    public void verifyOtp(String email, String code, Resultado result) {
        Map<String, String> body = emailBody(email);
        body.put("code", code);
        ejecutarConSesion(api.verifyOtp(body), result);
    }

    public void logout(Resultado result) {
        sessionManager.clear();
        ejecutar(api.logout(), result);
    }

    private Map<String, String> emailBody(String email) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        return body;
    }

    private void ejecutarConSesion(Call<LoginResponse> call, Resultado result) {
        call.enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getSession() != null) {
                    sessionManager.saveSession(body);
                    result.onSuccess();
                } else {
                    result.onError("El servidor respondió con código " + response.code());
                }
            }
            @Override public void onFailure(Call<LoginResponse> call, Throwable error) {
                result.onError(error instanceof IOException ? "No se pudo conectar con el servidor" : "Respuesta inválida del servidor");
            }
        });
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
