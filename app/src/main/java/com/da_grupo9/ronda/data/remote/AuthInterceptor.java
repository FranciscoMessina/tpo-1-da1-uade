package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.local.SessionExpirationNotifier;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;
    private final SessionExpirationNotifier sessionExpirationNotifier;

    @Inject
    public AuthInterceptor(
            SessionManager sessionManager,
            SessionExpirationNotifier sessionExpirationNotifier
    ) {
        this.sessionManager = sessionManager;
        this.sessionExpirationNotifier = sessionExpirationNotifier;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getToken();

        if (token == null) {
            return chain.proceed(original);
        }

        Request authorized = original.newBuilder()
                .header("Authorization", sessionManager.getTokenType() + " " + token)
                .build();
        Response response = chain.proceed(authorized);
        if (response.code() == 401 && sessionManager.clearIfTokenMatches(token)) {
            sessionExpirationNotifier.notifySessionExpired();
        }
        return response;
    }
}
