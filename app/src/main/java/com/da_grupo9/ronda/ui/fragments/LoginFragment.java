package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.local.SessionManager;
import com.da_grupo9.ronda.data.repository.AuthRepository;
import com.da_grupo9.ronda.ui.components.BiometricActivationHelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.navigation.Navigation;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private static final int AUTHENTICATORS =
            Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL;

    @Inject AuthRepository authRepository;
    @Inject SessionManager sessionManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_login,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        EditText editPassword =
                view.findViewById(R.id.editPassword);

        EditText editEmail =
                view.findViewById(R.id.editEmail);

        Button buttonLogin =
                view.findViewById(R.id.buttonLogin);

        Button buttonLoginOtp =
                view.findViewById(R.id.buttonLoginOtp);

        Button buttonForgotPassword =
                view.findViewById(R.id.buttonForgotPassword);

        Button buttonCreateAccount = view.findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> {

            String email =
                    editEmail.getText().toString();

            String password =
                    editPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Los campos son obligatorios",
                        Toast.LENGTH_SHORT
                ).show();

            } else {
                authRepository.login(email, password, new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        ofrecerBiometria(v);
                    }

                    @Override public void onError(String mensaje) {
                        mostrarError(mensaje);
                    }
                });
            }
        });

        buttonLoginOtp.setOnClickListener(v -> {

            String email =
                    editEmail.getText().toString();

            if (email.isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Ingresá tu email para recibir el código",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                authRepository.requestOtp(email, "login", new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        if (!isAdded()) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        bundle.putString("proposito", "login");
                        Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_otpFragment, bundle);
                    }
                    @Override public void onError(String mensaje) { mostrarError(mensaje); }
                });
            }
        });

        buttonForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

        buttonCreateAccount.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_loginFragment_to_registerFragment));

        if (sessionManager.isLoggedIn() && sessionManager.isBiometricEnabled()) {
            mostrarBiometria(view);
        }
    }

    private void ofrecerBiometria(View view) {
        BiometricActivationHelper.offer(this, sessionManager, () -> irAlHome(view));
    }

    private void mostrarBiometria(View view) {
        BiometricManager manager = BiometricManager.from(requireContext());
        if (manager.canAuthenticate(AUTHENTICATORS) != BiometricManager.BIOMETRIC_SUCCESS) {
            mostrarError("La autenticación biométrica no está disponible");
            return;
        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Ingresar a Ronda")
                .setSubtitle("Confirmá tu identidad para continuar")
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build();

        BiometricPrompt prompt = new BiometricPrompt(this,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        authRepository.validarSesionGuardada(navegarAlHome(view));
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        mostrarError("No se pudo verificar la identidad");
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }
                });

        prompt.authenticate(promptInfo);
    }

    private AuthRepository.Resultado navegarAlHome(View view) {
        return new AuthRepository.Resultado() {
            @Override public void onSuccess() {
                irAlHome(view);
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        };
    }

    private void irAlHome(View view) {
        if (!isAdded()) return;
        Navigation.findNavController(view)
                .navigate(R.id.action_loginFragment_to_homeFragment);
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
