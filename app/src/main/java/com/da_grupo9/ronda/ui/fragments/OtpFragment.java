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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpFragment extends Fragment {
    @Inject AuthRepository authRepository;
    @Inject SessionManager sessionManager;

    public OtpFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_otp,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView textEmail =
                view.findViewById(R.id.textOtpEmail);

        EditText editOtp =
                view.findViewById(R.id.editOtp);

        Button buttonConfirm =
                view.findViewById(R.id.buttonConfirmOtp);

        Button buttonResend =
                view.findViewById(R.id.buttonResendOtp);

        String email = "";
        String purpose = "login";
        String name = "";
        String username = "";
        String phone = "";
        String zone = "";
        String password = "";

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
            purpose = getArguments().getString("purpose", "login");
            name = getArguments().getString("name", "");
            username = getArguments().getString("username", "");
            phone = getArguments().getString("phone", "");
            zone = getArguments().getString("zone", "");
            password = getArguments().getString("password", "");
        }

        textEmail.setText(
                "Ingresá el código enviado a " + email
        );

        String finalEmail = email;
        String finalPurpose = purpose;
        String finalName = name;
        String finalUsername = username;
        String finalPhone = phone;
        String finalZone = zone;
        String finalPassword = password;

        buttonConfirm.setOnClickListener(v -> {

            String code =
                    editOtp.getText().toString();

            if (!code.matches("\\d{6}")) {

                Toast.makeText(
                        requireContext(),
                        "Ingresá los 6 dígitos del código",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                AuthRepository.Resultado resultado = new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        if (!isAdded()) return;
                        ofrecerBiometria(v, finalEmail);
                    }
                    @Override public void onError(String mensaje) { mostrarError(mensaje); }
                };
                if ("registration".equals(finalPurpose)) {
                    authRepository.verifyRegistration(finalEmail, code, finalName, finalUsername,
                            finalPhone, finalZone, finalPassword, resultado);
                } else {
                    authRepository.verifyLoginOtp(finalEmail, code, resultado);
                }
            }
        });

        buttonResend.setOnClickListener(v -> authRepository.resendOtp(finalEmail, finalPurpose, new AuthRepository.Resultado() {
            @Override public void onSuccess() {
                if (isAdded()) Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        }));
    }

    private void ofrecerBiometria(View view, String email) {
        BiometricActivationHelper.offer(this, sessionManager, () -> navegarAlHome(view, email));
    }

    private void navegarAlHome(View view, String email) {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        Navigation.findNavController(view)
                .navigate(R.id.action_otpFragment_to_homeFragment, bundle);
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
