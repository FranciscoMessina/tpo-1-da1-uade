package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.da_grupo9.ronda.data.repository.AuthRepository;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {
    @Inject AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_forgot_password,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        EditText editEmail = view.findViewById(R.id.editForgotEmail);
        Button buttonSend = view.findViewById(R.id.buttonSendRecoveryCode);

        buttonSend.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Ingresá tu email para recuperar el acceso",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            authRepository.requestOtp(email, "set_password", new AuthRepository.Resultado() {
                @Override public void onSuccess() {
                    if (!isAdded()) return;
                    Bundle arguments = new Bundle();
                    arguments.putString("email", email);
                    arguments.putBoolean("volverAlPerfil", false);
                    Navigation.findNavController(v).navigate(
                            R.id.action_forgotPasswordFragment_to_resetPasswordFragment, arguments);
                }

                @Override public void onError(String mensaje) {
                    if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
