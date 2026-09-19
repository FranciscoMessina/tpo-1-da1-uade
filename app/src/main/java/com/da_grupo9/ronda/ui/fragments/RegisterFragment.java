package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {
    @Inject AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText email = view.findViewById(R.id.editRegisterEmail);
        EditText name = view.findViewById(R.id.editRegisterName);
        EditText username = view.findViewById(R.id.editRegisterUsername);
        EditText phone = view.findViewById(R.id.editRegisterPhone);
        EditText zone = view.findViewById(R.id.editRegisterZone);
        EditText password = view.findViewById(R.id.editRegisterPassword);
        EditText passwordConfirm = view.findViewById(R.id.editRegisterPasswordConfirm);

        view.findViewById(R.id.buttonRequestRegistrationOtp).setOnClickListener(v -> {
            String emailValue = email.getText().toString().trim();
            String nameValue = name.getText().toString().trim();
            String usernameValue = username.getText().toString().trim();
            String phoneValue = phone.getText().toString().trim();
            String zoneValue = zone.getText().toString().trim();
            String passwordValue = password.getText().toString();
            String passwordConfirmValue = passwordConfirm.getText().toString();

            if (emailValue.isEmpty() || nameValue.length() < 2 || usernameValue.length() < 3 || zoneValue.isEmpty()) {
                Toast.makeText(requireContext(), "Completá email, nombre, usuario y zona", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!usernameValue.matches("[a-zA-Z0-9_.-]{3,30}")) {
                Toast.makeText(requireContext(), "El usuario sólo admite letras, números, _, . y -", Toast.LENGTH_LONG).show();
                return;
            }
            if (!passwordValue.isEmpty() && (passwordValue.length() < 8 || passwordValue.length() > 72)) {
                Toast.makeText(requireContext(), "La contraseña debe tener entre 8 y 72 caracteres", Toast.LENGTH_LONG).show();
                return;
            }
            if (!passwordValue.equals(passwordConfirmValue)) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            authRepository.requestOtp(emailValue, "registration", new AuthRepository.Resultado() {
                @Override public void onSuccess() {
                    if (!isAdded()) return;
                    Bundle args = new Bundle();
                    args.putString("email", emailValue);
                    args.putString("purpose", "registration");
                    args.putString("name", nameValue);
                    args.putString("username", usernameValue);
                    args.putString("phone", phoneValue);
                    args.putString("zone", zoneValue);
                    args.putString("password", passwordValue);
                    Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_otpFragment, args);
                }

                @Override public void onError(String mensaje) {
                    if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
