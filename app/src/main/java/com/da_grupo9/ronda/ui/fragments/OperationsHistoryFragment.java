package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.model.Operation;
import com.da_grupo9.ronda.data.model.OperationsResponse;
import com.da_grupo9.ronda.data.repository.ProfileRepository;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.ui.components.RatingBottomSheet;
import com.da_grupo9.ronda.util.OperationFormat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OperationsHistoryFragment extends Fragment {

    private static final String[] TYPES = {Operation.TYPE_BUY, Operation.TYPE_SELL};

    @Inject ProfileRepository profileRepository;

    // El estado vive en el fragment para conservar pestaña y filtros al volver del detalle.
    private int selectedTab;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int requestId;
    private final List<Operation> operations = new ArrayList<>();

    private LinearLayout container;
    private LinearProgressIndicator progress;
    private TextView textMessage;
    private MaterialButton buttonRetry;
    private MaterialButton buttonFrom;
    private MaterialButton buttonTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operations_history, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = view.findViewById(R.id.containerOperations);
        progress = view.findViewById(R.id.progressHistory);
        textMessage = view.findViewById(R.id.textHistoryMessage);
        buttonRetry = view.findViewById(R.id.buttonHistoryRetry);
        buttonFrom = view.findViewById(R.id.buttonFilterFrom);
        buttonTo = view.findViewById(R.id.buttonFilterTo);

        view.findViewById(R.id.buttonHistoryBack).setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack());

        TabLayout tabs = view.findViewById(R.id.historyTabs);
        tabs.addTab(tabs.newTab().setText("Compras"));
        tabs.addTab(tabs.newTab().setText("Ventas"));
        TabLayout.Tab initial = tabs.getTabAt(selectedTab);
        if (initial != null) initial.select();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                cargar();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        buttonFrom.setOnClickListener(v -> elegirFecha(true));
        buttonTo.setOnClickListener(v -> elegirFecha(false));
        view.findViewById(R.id.buttonFilterClear).setOnClickListener(v -> {
            if (fromDate == null && toDate == null) return;
            fromDate = null;
            toDate = null;
            actualizarFiltros();
            cargar();
        });
        buttonRetry.setOnClickListener(v -> cargar());

        getChildFragmentManager().setFragmentResultListener(RatingBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(), (key, result) -> {
                    if (result.getBoolean(RatingBottomSheet.RESULT_REFRESH_ONLY, false)) {
                        cargar();
                        return;
                    }
                    String id = result.getString(RatingBottomSheet.RESULT_OPERATION_ID);
                    for (Operation operation : operations) {
                        if (operation.getId() != null && operation.getId().equals(id)) {
                            operation.setMyRating(result.getInt(RatingBottomSheet.RESULT_RATING));
                        }
                    }
                    renderizar();
                });

        actualizarFiltros();
        cargar();
    }

    private void elegirFecha(boolean esDesde) {
        LocalDate actual = esDesde ? fromDate : toDate;
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(esDesde ? "Fecha desde" : "Fecha hasta")
                .setSelection(actual == null
                        ? MaterialDatePicker.todayInUtcMilliseconds()
                        : actual.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            // El picker informa la fecha elegida como medianoche UTC.
            LocalDate fecha = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate nuevoDesde = esDesde ? fecha : fromDate;
            LocalDate nuevoHasta = esDesde ? toDate : fecha;
            if (nuevoDesde != null && nuevoHasta != null && nuevoDesde.isAfter(nuevoHasta)) {
                Toast.makeText(requireContext(), "La fecha desde no puede ser posterior a la fecha hasta",
                        Toast.LENGTH_LONG).show();
                return;
            }
            fromDate = nuevoDesde;
            toDate = nuevoHasta;
            actualizarFiltros();
            cargar();
        });
        picker.show(getChildFragmentManager(), "history_date_picker");
    }

    private void actualizarFiltros() {
        DateTimeFormatter format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        buttonFrom.setText(fromDate == null ? "Desde" : "Desde: " + fromDate.format(format));
        buttonTo.setText(toDate == null ? "Hasta" : "Hasta: " + toDate.format(format));
    }

    private void cargar() {
        int currentRequest = ++requestId;
        operations.clear();
        container.removeAllViews();
        textMessage.setVisibility(View.GONE);
        buttonRetry.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        ZoneId zone = ZoneId.systemDefault();
        String from = fromDate == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(fromDate.atStartOfDay(zone));
        String to = toDate == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(toDate.atTime(23, 59, 59).atZone(zone));

        profileRepository.getOperations(TYPES[selectedTab], from, to,
                new RepositoryResult<OperationsResponse>() {
                    @Override public void onSuccess(OperationsResponse response) {
                        // Se descartan respuestas de una pestaña o filtro que ya cambió.
                        if (!isAdded() || getView() == null || currentRequest != requestId) return;
                        progress.setVisibility(View.GONE);
                        operations.addAll(response.getItems());
                        renderizar();
                    }

                    @Override public void onError(String mensaje) {
                        if (!isAdded() || getView() == null || currentRequest != requestId) return;
                        progress.setVisibility(View.GONE);
                        textMessage.setText(mensaje);
                        textMessage.setVisibility(View.VISIBLE);
                        buttonRetry.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void renderizar() {
        container.removeAllViews();
        if (operations.isEmpty()) {
            textMessage.setText(selectedTab == 0
                    ? "No tenés compras para mostrar." : "No tenés ventas para mostrar.");
            textMessage.setVisibility(View.VISIBLE);
            return;
        }
        textMessage.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Operation operation : operations) {
            View card = inflater.inflate(R.layout.item_operation, container, false);
            ((TextView) card.findViewById(R.id.textOperationMeta)).setText(
                    OperationFormat.typeLabel(operation) + " · " + OperationFormat.date(operation.getCompletedAt()));
            ((TextView) card.findViewById(R.id.textOperationTitle)).setText(operation.getTitle());
            ((TextView) card.findViewById(R.id.textOperationAmount)).setText(OperationFormat.amount(operation.getAmount()));
            ((TextView) card.findViewById(R.id.textOperationCounterparty)).setText(
                    OperationFormat.counterpartyRole(operation) + ": " + OperationFormat.counterpartyLabel(operation));
            ((TextView) card.findViewById(R.id.textOperationRating)).setText(OperationFormat.ratingStatus(operation));

            View.OnClickListener abrirDetalle = v -> {
                Bundle args = new Bundle();
                args.putSerializable("operacion", operation);
                Navigation.findNavController(v).navigate(
                        R.id.action_operationsHistoryFragment_to_operationDetailFragment, args);
            };
            card.setOnClickListener(abrirDetalle);

            View buttonRate = card.findViewById(R.id.buttonOperationRate);
            buttonRate.setVisibility(operation.canRate() ? View.VISIBLE : View.GONE);
            buttonRate.setOnClickListener(v -> RatingBottomSheet
                    .newInstance(operation.getId(), OperationFormat.counterpartyLabel(operation))
                    .show(getChildFragmentManager(), RatingBottomSheet.TAG));

            container.addView(card);
        }
    }
}
