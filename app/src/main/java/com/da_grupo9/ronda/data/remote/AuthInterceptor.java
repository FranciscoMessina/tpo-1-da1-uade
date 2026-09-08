package com.da_grupo9.ronda.data.remote;

import com.da_grupo9.ronda.data.local.SessionManager;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    @Inject
    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
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
        return chain.proceed(authorized);
    }
}
