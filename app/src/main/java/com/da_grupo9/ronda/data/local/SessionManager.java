package com.da_grupo9.ronda.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.da_grupo9.ronda.data.model.LoginResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PASSWORD_KNOWN_PREFIX = "password_known_";
    private static final String KEY_HAS_PASSWORD_PREFIX = "has_password_";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    private final SharedPreferences prefs;

    @Inject
    public SessionManager(@ApplicationContext Context context) {
        prefs = createEncryptedPrefs(context);
    }

    private static SharedPreferences createEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento seguro de sesión", e);
        }
    }

    public void saveSession(LoginResponse response) {
        prefs.edit()
                .putString(KEY_TOKEN, response.getSession().getToken())
                .putString(KEY_TOKEN_TYPE, response.getSession().getTokenType())
                .putString(KEY_USER_ID, response.getUserId())
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getTokenType() {
        return prefs.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void setPasswordStatus(boolean hasPassword) {
        String userId = getUserId();
        if (userId == null) return;
        prefs.edit()
                .putBoolean(KEY_PASSWORD_KNOWN_PREFIX + userId, true)
                .putBoolean(KEY_HAS_PASSWORD_PREFIX + userId, hasPassword)
                .apply();
    }

    public boolean isPasswordStatusKnown() {
        String userId = getUserId();
        return userId != null && prefs.getBoolean(KEY_PASSWORD_KNOWN_PREFIX + userId, false);
    }

    public boolean hasPassword() {
        String userId = getUserId();
        return userId != null && prefs.getBoolean(KEY_HAS_PASSWORD_PREFIX + userId, false);
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_TOKEN_TYPE)
                .remove(KEY_USER_ID)
                .remove(KEY_BIOMETRIC_ENABLED)
                .apply();
    }

    public synchronized boolean clearIfTokenMatches(String token) {
        if (token == null || !token.equals(getToken())) return false;
        clear();
        return true;
    }
}
