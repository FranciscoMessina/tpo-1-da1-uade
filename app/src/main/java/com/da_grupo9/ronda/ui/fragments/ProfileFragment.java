package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.ProfileRepository;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.data.repository.AuthRepository;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.da_grupo9.ronda.data.model.Perfil;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    @Inject ProfileRepository profileRepository;
    @Inject AuthRepository authRepository;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        EditText editNombre =
                view.findViewById(R.id.editNombre);

        EditText editEmail =
                view.findViewById(R.id.editEmailPerfil);

        EditText editTelefono =
                view.findViewById(R.id.editTelefono);

        EditText editZona =
                view.findViewById(R.id.editZona);

        Button buttonGuardar =
                view.findViewById(R.id.buttonGuardarPerfil);

        Button buttonLogout =
                view.findViewById(R.id.buttonLogout);

        profileRepository.getMe(new PublicacionRepository.Resultado<Perfil>() {
            @Override public void onSuccess(Perfil perfil) {
                if (!isAdded()) return;
                editNombre.setText(perfil.getNombre());
                editEmail.setText(perfil.getEmail());
                editTelefono.setText(perfil.getTelefono());
                editZona.setText(perfil.getZona());
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        });

        buttonGuardar.setOnClickListener(v -> {

            String nombre =
                    editNombre.getText().toString().trim();

            String email =
                    editEmail.getText().toString().trim();

            String telefono =
                    editTelefono.getText().toString().trim();

            String zona =
                    editZona.getText().toString().trim();

            if (nombre.isEmpty()
                    || email.isEmpty()
                    || telefono.isEmpty()
                    || zona.isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "Todos los campos son obligatorios",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Perfil perfil = new Perfil(nombre, email, telefono, zona);
                profileRepository.updateMe(perfil, new PublicacionRepository.Resultado<Perfil>() {
                    @Override public void onSuccess(Perfil data) {
                        if (isAdded()) Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String mensaje) { mostrarError(mensaje); }
                });
            }
        });

        buttonLogout.setOnClickListener(v -> {
            buttonLogout.setEnabled(false);
            authRepository.logout(new AuthRepository.Resultado() {
                @Override public void onSuccess() {
                    if (!isAdded()) return;
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build();
                    Navigation.findNavController(v).navigate(R.id.loginFragment, null, options);
                }

                @Override public void onError(String mensaje) {
                    if (!isAdded()) return;
                    buttonLogout.setEnabled(true);
                    mostrarError(mensaje);
                }
            });
        });
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
