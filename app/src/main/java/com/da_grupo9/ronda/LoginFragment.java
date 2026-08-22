package com.da_grupo9.ronda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Constructor vacío obligatorio
    }

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


        // LOGIN CON EMAIL Y CONTRASEÑA
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

                Toast.makeText(
                        requireContext(),
                        "Felicitaciones te logeaste con " + email,
                        Toast.LENGTH_LONG
                ).show();

                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });


        // LOGIN MEDIANTE OTP
        // Por ahora sigue utilizando OtpActivity.
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

                Toast.makeText(
                        requireContext(),
                        "Código enviado a " + email,
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent =
                        new Intent(
                                requireContext(),
                                OtpActivity.class
                        );

                intent.putExtra(
                        OtpActivity.EXTRA_EMAIL,
                        email
                );

                startActivity(intent);
            }
        });


        // RECUPERAR CONTRASEÑA
        // Por ahora sigue utilizando ForgotPasswordActivity.
        buttonForgotPassword.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            requireContext(),
                            ForgotPasswordActivity.class
                    );

            startActivity(intent);
        });
    }
}