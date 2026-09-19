package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.da_grupo9.ronda.data.repository.AuthRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordFragment extends Fragment {
    @Inject AuthRepository authRepository;

    public ResetPasswordFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_reset_password,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        String email = requireArguments().getString("email", "");
        boolean returnToProfile = requireArguments().getBoolean("returnToProfile", false);

        TextView textEmail = view.findViewById(R.id.textResetEmail);
        EditText editCode = view.findViewById(R.id.editResetCode);
        EditText editNewPassword = view.findViewById(R.id.editNewPassword);
        EditText editConfirmPassword = view.findViewById(R.id.editConfirmPassword);
        Button buttonReset = view.findViewById(R.id.buttonResetPassword);
        Button buttonResend = view.findViewById(R.id.buttonResendRecoveryCode);

        textEmail.setText(
                "Ingresá el código enviado a " + email
                        + " y elegí tu nueva contraseña"
        );

        buttonReset.setOnClickListener(v -> {
            String code = editCode.getText().toString();
            String newPassword = editNewPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();

            if (!code.matches("\\d{6}") || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Ingresá el código de 6 dígitos y la contraseña",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (newPassword.length() < 8 || newPassword.length() > 72) {
                Toast.makeText(requireContext(), "La contraseña debe tener entre 8 y 72 caracteres", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(
                        requireContext(),
                        "Las contraseñas no coinciden",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                buttonReset.setEnabled(false);
                authRepository.setPassword(email, code, newPassword, new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Contraseña guardada correctamente", Toast.LENGTH_LONG).show();
                        if (returnToProfile) Navigation.findNavController(v).popBackStack();
                        else Navigation.findNavController(v).navigate(R.id.action_resetPasswordFragment_to_homeFragment);
                    }

                    @Override public void onError(String mensaje) {
                        if (!isAdded()) return;
                        buttonReset.setEnabled(true);
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        buttonResend.setOnClickListener(v -> authRepository.resendOtp(email, "set_password", new AuthRepository.Resultado() {
            @Override public void onSuccess() {
                if (isAdded()) Toast.makeText(requireContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String mensaje) {
                if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        }));
    }
}
