package com.da_grupo9.ronda.data.repository;

import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.model.LoginResponse;
import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.remote.AuthApi;
import com.da_grupo9.ronda.data.remote.ProfileApi;
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
    private final ProfileApi profileApi;
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(AuthApi api, ProfileApi profileApi, SessionManager sessionManager) {
        this.api = api;
        this.profileApi = profileApi;
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
        if (!sessionManager.isLoggedIn()) {
            result.onSuccess();
            return;
        }

        // Se intenta invalidar la sesión remota mientras el interceptor todavía
        // puede adjuntar el token. La sesión local se elimina en todos los casos.
        api.logout().enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                sessionManager.clear();
                result.onSuccess();
            }

            @Override public void onFailure(Call<ResponseBody> call, Throwable error) {
                sessionManager.clear();
                result.onSuccess();
            }
        });
    }

    public void validarSesionGuardada(Resultado result) {
        if (!sessionManager.isLoggedIn()) {
            result.onError("No hay una sesión guardada");
            return;
        }

        profileApi.getMe().enqueue(new Callback<Perfil>() {
            @Override public void onResponse(Call<Perfil> call, Response<Perfil> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.onSuccess();
                    return;
                }

                if (response.code() == 401 || response.code() == 403) {
                    sessionManager.clear();
                }
                result.onError("La sesión guardada ya no es válida");
            }

            @Override public void onFailure(Call<Perfil> call, Throwable error) {
                result.onError("No se pudo validar la sesión guardada");
            }
        });
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
