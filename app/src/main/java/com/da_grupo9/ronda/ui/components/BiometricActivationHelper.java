package com.da_grupo9.ronda.ui.components;

import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.fragment.app.Fragment;

import com.da_grupo9.ronda.data.local.SessionManager;

/** Presenta una sola vez el ofrecimiento de biometría y continúa en cualquier respuesta. */
public final class BiometricActivationHelper {
    private static final int AUTHENTICATORS =
            Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL;

    private BiometricActivationHelper() {}

    public static void offer(Fragment fragment, SessionManager sessionManager, Runnable continuation) {
        if (!fragment.isAdded()) return;

        if (sessionManager.isBiometricEnabled()
                || BiometricManager.from(fragment.requireContext()).canAuthenticate(AUTHENTICATORS)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            continuation.run();
            return;
        }

        new AlertDialog.Builder(fragment.requireContext())
                .setTitle("Activar acceso biométrico")
                .setMessage("¿Querés usar la biometría la próxima vez que ingreses?")
                .setPositiveButton("Activar", (dialog, which) -> {
                    sessionManager.setBiometricEnabled(true);
                    continuation.run();
                })
                .setNegativeButton("Ahora no", (dialog, which) -> continuation.run())
                .show();
    }
}
