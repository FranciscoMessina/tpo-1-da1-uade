package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.AuthRepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    @Inject AuthRepository authRepository;

    public LoginFragment() {
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

                authRepository.login(email, password, navegarAlHome(v, email));
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

                authRepository.requestOtp(email, new AuthRepository.Resultado() {
                    @Override public void onSuccess() {
                        if (!isAdded()) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
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
    }

    private AuthRepository.Resultado navegarAlHome(View view, String email) {
        return new AuthRepository.Resultado() {
            @Override public void onSuccess() {
                if (!isAdded()) return;
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment, bundle);
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        };
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
