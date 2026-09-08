package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.AuthRepository;

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

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        textEmail.setText(
                "Ingresá el código enviado a " + email
        );

        String finalEmail = email;

        buttonConfirm.setOnClickListener(v -> {

            String code =
                    editOtp.getText().toString();

            if (code.isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Ingresá el código recibido",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                authRepository.verifyOtp(finalEmail, code, new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        if (!isAdded()) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("email", finalEmail);
                        Navigation.findNavController(v).navigate(R.id.action_otpFragment_to_homeFragment, bundle);
                    }
                    @Override public void onError(String mensaje) { mostrarError(mensaje); }
                });
            }
        });

        buttonResend.setOnClickListener(v -> authRepository.resendOtp(finalEmail, new AuthRepository.Resultado() {
            @Override public void onSuccess() {
                if (isAdded()) Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        }));
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
