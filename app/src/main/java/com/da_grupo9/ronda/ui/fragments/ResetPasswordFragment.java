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

public class ResetPasswordFragment extends Fragment {

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

            if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Todos los campos son obligatorios",
                        Toast.LENGTH_SHORT
                ).show();
            } else if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(
                        requireContext(),
                        "Las contraseñas no coinciden",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        requireContext(),
                        "Contraseña actualizada correctamente",
                        Toast.LENGTH_LONG
                ).show();

                Navigation.findNavController(v).navigate(
                        R.id.action_resetPasswordFragment_to_loginFragment
                );
            }
        });

        buttonResend.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Código reenviado a " + email,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}
