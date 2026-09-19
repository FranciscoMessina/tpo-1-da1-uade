package com.da_grupo9.ronda.ui.fragments;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.ProfileRepository;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.data.repository.AuthRepository;
import com.da_grupo9.ronda.data.local.SessionManager;

import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import com.da_grupo9.ronda.data.model.Perfil;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.model.UploadImageResponse;
import com.bumptech.glide.Glide;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    @Inject ProfileRepository profileRepository;
    @Inject AuthRepository authRepository;
    @Inject PublicacionRepository publicacionRepository;
    @Inject SessionManager sessionManager;
    private ImageView imageAvatar;
    private Button buttonCambiarAvatar;
    private String avatarUrlActual;
    private String currentEmail;
    private TextView textPasswordStatus;
    private Button buttonSetPassword;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) subirAvatar(uri);
            });

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

        imageAvatar = view.findViewById(R.id.imageAvatarPerfil);
        buttonCambiarAvatar = view.findViewById(R.id.buttonCambiarAvatar);
        TextView textReputacion = view.findViewById(R.id.textReputacion);
        TextView textOperaciones = view.findViewById(R.id.textOperaciones);
        TextView textCompradorVendedor = view.findViewById(R.id.textCompradorVendedor);
        TextView textAntiguedad = view.findViewById(R.id.textAntiguedad);
        TextView textPublicacionesActivas = view.findViewById(R.id.textPublicacionesActivas);
        textPasswordStatus = view.findViewById(R.id.textPasswordStatus);
        buttonSetPassword = view.findViewById(R.id.buttonSetPassword);
        mostrarEstadoPassword(textPasswordStatus, buttonSetPassword);

        Button buttonLogout =
                view.findViewById(R.id.buttonLogout);

        view.findViewById(R.id.buttonMyOffers).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        R.id.action_profileFragment_to_offersFragment));

        view.findViewById(R.id.buttonOperationsHistory).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        R.id.action_profileFragment_to_operationsHistoryFragment));

        profileRepository.getMe(new PublicacionRepository.Resultado<Perfil>() {
            @Override public void onSuccess(Perfil perfil) {
                if (!isAdded()) return;
                editNombre.setText(perfil.getNombre());
                editEmail.setText(perfil.getEmail());
                currentEmail = perfil.getEmail();
                buttonSetPassword.setEnabled(currentEmail != null && !currentEmail.isEmpty());
                if (perfil.getHasPassword() != null) {
                    sessionManager.setPasswordStatus(perfil.getHasPassword());
                    mostrarEstadoPassword(textPasswordStatus, buttonSetPassword);
                }
                editTelefono.setText(perfil.getTelefono());
                editZona.setText(perfil.getZona());
                avatarUrlActual = perfil.getAvatarUrl();
                mostrarAvatar(avatarUrlActual);
                PublicUser reputacion = perfil.getReputation();
                if (reputacion != null) {
                    textReputacion.setText(String.format(Locale.getDefault(), "%.1f estrellas (%d calificaciones)",
                            reputacion.getRatingAverage(), reputacion.getRatingCount()));
                    int compras = reputacion.getPurchasesCompleted();
                    int ventas = reputacion.getSalesCompleted();
                    textOperaciones.setText((compras + ventas) + " operaciones concretadas");
                    textCompradorVendedor.setText(compras + " como comprador · " + ventas + " como vendedor");
                    textAntiguedad.setText("Miembro desde: " + formatearFecha(reputacion.getMemberSince()));
                } else {
                    textReputacion.setText("Sin calificaciones");
                    textOperaciones.setText("0 operaciones concretadas");
                    textCompradorVendedor.setText("0 como comprador · 0 como vendedor");
                    textAntiguedad.setText("Miembro desde: " + formatearFecha(perfil.getCreatedAt()));
                }
            }
            @Override public void onError(String mensaje) { mostrarError(mensaje); }
        });

        publicacionRepository.getMisPublicaciones(new PublicacionRepository.Resultado<List<Publicacion>>() {
            @Override public void onSuccess(List<Publicacion> publicaciones) {
                if (!isAdded()) return;
                long activas = publicaciones.stream().filter(Publicacion::isVisibleInPublicFeed).count();
                textPublicacionesActivas.setText("Publicaciones activas: " + activas);
            }
            @Override public void onError(String mensaje) {
                if (isAdded()) textPublicacionesActivas.setText("Publicaciones activas: no disponible");
            }
        });

        buttonCambiarAvatar.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        buttonSetPassword.setOnClickListener(v -> {
            if (currentEmail == null || currentEmail.isEmpty()) return;
            buttonSetPassword.setEnabled(false);
            authRepository.requestOtp(currentEmail, "set_password", new AuthRepository.Resultado() {
                @Override public void onSuccess() {
                    if (!isAdded()) return;
                    buttonSetPassword.setEnabled(true);
                    Bundle args = new Bundle();
                    args.putString("email", currentEmail);
                    args.putBoolean("returnToProfile", true);
                    Navigation.findNavController(v).navigate(R.id.resetPasswordFragment, args);
                }

                @Override public void onError(String mensaje) {
                    if (!isAdded()) return;
                    buttonSetPassword.setEnabled(true);
                    mostrarError(mensaje);
                }
            });
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

                Perfil perfil = new Perfil(nombre, email, telefono, zona, avatarUrlActual);
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
                    mostrarError(mensaje);
                    NavOptions options = new NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build();
                    Navigation.findNavController(v).navigate(R.id.loginFragment, null, options);
                }
            });
        });
    }

    private void mostrarError(String mensaje) {
        if (isAdded()) Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void mostrarEstadoPassword(TextView status, Button button) {
        if (!sessionManager.isPasswordStatusKnown()) {
            status.setText("No podemos confirmar si esta cuenta tiene contraseña. Podés agregar una nueva para asegurar el acceso.");
            button.setText("Agregar contraseña");
        } else if (sessionManager.hasPassword()) {
            status.setText("Tu cuenta tiene una contraseña configurada.");
            button.setText("Cambiar contraseña");
        } else {
            status.setText("Sólo podrás entrar con OTP hasta que agregues una contraseña.");
            button.setText("Agregar contraseña");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textPasswordStatus != null && buttonSetPassword != null) {
            mostrarEstadoPassword(textPasswordStatus, buttonSetPassword);
        }
    }

    private void subirAvatar(Uri uri) {
        try (InputStream input = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalArgumentException("No se pudo leer la imagen");
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > 5 * 1024 * 1024) {
                    mostrarError("La imagen no puede superar los 5 MB");
                    return;
                }
                output.write(buffer, 0, read);
            }
            String mime = requireContext().getContentResolver().getType(uri);
            if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/webp"))) {
                mostrarError("Elegí una imagen JPG, PNG o WebP");
                return;
            }
            buttonCambiarAvatar.setEnabled(false);
            RequestBody body = RequestBody.create(output.toByteArray(), MediaType.parse(mime));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "avatar", body);
            profileRepository.uploadAvatar(part, new PublicacionRepository.Resultado<UploadImageResponse>() {
                @Override public void onSuccess(UploadImageResponse upload) {
                    guardarAvatar(upload.getUrl());
                }
                @Override public void onError(String mensaje) {
                    if (isAdded()) buttonCambiarAvatar.setEnabled(true);
                    mostrarError(mensaje);
                }
            });
        } catch (Exception error) {
            mostrarError("No se pudo leer la imagen seleccionada");
        }
    }

    private void guardarAvatar(String url) {
        profileRepository.updateMe(new Perfil(null, null, null, null, url),
                new PublicacionRepository.Resultado<Perfil>() {
                    @Override public void onSuccess(Perfil perfil) {
                        if (!isAdded()) return;
                        avatarUrlActual = perfil.getAvatarUrl() != null ? perfil.getAvatarUrl() : url;
                        mostrarAvatar(avatarUrlActual);
                        buttonCambiarAvatar.setEnabled(true);
                        Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String mensaje) {
                        if (isAdded()) buttonCambiarAvatar.setEnabled(true);
                        mostrarError(mensaje);
                    }
                });
    }

    private void mostrarAvatar(String url) {
        if (!isAdded() || imageAvatar == null || url == null || url.isEmpty()) return;
        Glide.with(this).load(url).placeholder(R.drawable.ic_photo_camera).error(R.drawable.ic_photo_camera).into(imageAvatar);
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return "sin datos";
        try {
            return OffsetDateTime.parse(fecha).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        } catch (RuntimeException ignored) {
            return fecha;
        }
    }
}
