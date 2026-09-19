package com.da_grupo9.ronda.ui.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Operation;
import com.da_grupo9.ronda.data.model.PublicUser;
import com.da_grupo9.ronda.data.repository.ProfileRepository;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.ui.components.RatingBottomSheet;
import com.da_grupo9.ronda.util.OperationFormat;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OperationDetailFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;
    @Inject ProfileRepository profileRepository;

    private Operation operation;
    private String counterpartyName;
    private String counterpartyReputation = "";

    private TextView textCounterparty;
    private TextView textReputation;
    private TextView textRating;
    private MaterialButton buttonRate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonDetailBack).setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack());

        operation = getArguments() == null ? null
                : BundleCompat.getSerializable(getArguments(), "operation", Operation.class);
        if (operation == null) {
            Toast.makeText(requireContext(), "No se pudo cargar la operación", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        textCounterparty = view.findViewById(R.id.textOpDetailCounterparty);
        textReputation = view.findViewById(R.id.textOpDetailReputation);
        textRating = view.findViewById(R.id.textOpDetailRating);
        buttonRate = view.findViewById(R.id.buttonOpDetailRate);
        MaterialButton buttonDirections = view.findViewById(R.id.buttonOpDetailDirections);
        MaterialButton buttonProfile = view.findViewById(R.id.buttonOpDetailProfile);
        TextView textAddress = view.findViewById(R.id.textOpDetailAddress);

        ((TextView) view.findViewById(R.id.textOpDetailType)).setText(OperationFormat.typeLabel(operation));
        ((TextView) view.findViewById(R.id.textOpDetailTitle)).setText(operation.getTitle());
        ((TextView) view.findViewById(R.id.textOpDetailAmount)).setText(OperationFormat.amount(operation.getAmount()));
        ((TextView) view.findViewById(R.id.textOpDetailDate)).setText(
                "Fecha: " + OperationFormat.date(operation.getCompletedAt()));

        boolean hasAddress = operation.getAddress() != null && !operation.getAddress().trim().isEmpty();
        textAddress.setVisibility(hasAddress ? View.VISIBLE : View.GONE);
        buttonDirections.setVisibility(hasAddress ? View.VISIBLE : View.GONE);
        if (hasAddress) {
            textAddress.setText("Dirección: " + operation.getAddress());
            buttonDirections.setOnClickListener(v -> abrirMapa(operation.getAddress()));
        }

        counterpartyName = OperationFormat.counterpartyLabel(operation);
        String counterpartyId = operation.getCounterpartyId();
        boolean hasCounterparty = counterpartyId != null && !counterpartyId.isEmpty();
        buttonProfile.setEnabled(hasCounterparty);
        buttonProfile.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("vendedorNombre", counterpartyName);
            args.putString("vendedorEmail", counterpartyId);
            args.putString("vendedorReputacion", counterpartyReputation);
            Navigation.findNavController(v).navigate(
                    R.id.action_operationDetailFragment_to_publicProfileFragment, args);
        });

        buttonRate.setOnClickListener(v -> RatingBottomSheet
                .newInstance(operation.getId(), counterpartyName)
                .show(getChildFragmentManager(), RatingBottomSheet.TAG));
        getChildFragmentManager().setFragmentResultListener(RatingBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(), (key, result) -> {
                    if (!result.getBoolean(RatingBottomSheet.RESULT_REFRESH_ONLY, false)) {
                        operation.setMyRating(result.getInt(RatingBottomSheet.RESULT_RATING));
                    }
                    mostrarCalificacion();
                    cargarContraparte();
                    refrescarOperacion();
                });

        mostrarContraparte();
        mostrarCalificacion();
        cargarContraparte();
    }

    /** Sincroniza con el servidor la calificación recién enviada; si falla queda la actualización local. */
    private void refrescarOperacion() {
        profileRepository.getOperation(operation.getId(), new PublicacionRepository.Resultado<Operation>() {
            @Override public void onSuccess(Operation fresh) {
                if (!isAdded() || getView() == null) return;
                operation = fresh;
                mostrarCalificacion();
            }

            @Override public void onError(String mensaje) { }
        });
    }

    private void mostrarContraparte() {
        textCounterparty.setText(OperationFormat.counterpartyRole(operation) + ": " + counterpartyName);
        textReputation.setText(counterpartyReputation.isEmpty() ? "Cargando reputación…"
                : "Reputación: " + counterpartyReputation);
    }

    private void mostrarCalificacion() {
        textRating.setText(OperationFormat.ratingStatus(operation));
        buttonRate.setVisibility(operation.canRate() ? View.VISIBLE : View.GONE);
    }

    /** Trae el nombre y la reputación vigente de la contraparte (también luego de calificarla). */
    private void cargarContraparte() {
        String id = operation.getCounterpartyId();
        if (id == null || id.isEmpty()) return;
        publicacionRepository.getUsuario(id, new PublicacionRepository.Resultado<PublicUser>() {
            @Override public void onSuccess(PublicUser user) {
                if (!isAdded() || getView() == null) return;
                counterpartyReputation = String.format(Locale.getDefault(), "%.1f (%d calificaciones)",
                        user.getRatingAverage(), user.getRatingCount());
                mostrarContraparte();
            }

            @Override public void onError(String mensaje) {
                if (!isAdded() || getView() == null) return;
                if (counterpartyReputation.isEmpty()) textReputation.setText("Reputación no disponible");
            }
        });
    }

    private void abrirMapa(String address) {
        Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, geo));
        } catch (ActivityNotFoundException ignored) {
            Uri web = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, web));
            } catch (ActivityNotFoundException error) {
                Toast.makeText(requireContext(), "No hay ninguna app para abrir el mapa", Toast.LENGTH_LONG).show();
            }
        }
    }
}
