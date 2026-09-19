package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Offer;
import com.da_grupo9.ronda.data.model.OfferActionResponse;
import com.da_grupo9.ronda.data.repository.OffersRepository;
import com.da_grupo9.ronda.util.ApiError;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OffersFragment extends Fragment {
    @Inject OffersRepository repository;

    private LinearLayout container;
    private TextView emptyView;
    private LinearProgressIndicator progress;
    private MaterialButton refreshButton;
    private final List<Offer> offers = new ArrayList<>();
    private String selectedRole = "buyer";
    private boolean loading;
    private boolean firstResume = true;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_offers, parent, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        Bundle args = getArguments();
        if (args != null) selectedRole = args.getString("selectedRole", "buyer");
        container = view.findViewById(R.id.containerOffers);
        emptyView = view.findViewById(R.id.textOffersEmpty);
        progress = view.findViewById(R.id.progressOffers);
        refreshButton = view.findViewById(R.id.buttonRefreshOffers);
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleOfferRole);
        toggle.check("seller".equals(selectedRole) ? R.id.buttonOffersReceived : R.id.buttonOffersSent);
        toggle.addOnButtonCheckedListener((group, checkedId, checked) -> {
            if (!checked) return;
            selectedRole = checkedId == R.id.buttonOffersReceived ? "seller" : "buyer";
            renderOffers();
        });
        view.findViewById(R.id.buttonOffersBack).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());
        refreshButton.setOnClickListener(v -> loadOffers());
        loadOffers();
    }

    @Override public void onResume() {
        super.onResume();
        if (firstResume) firstResume = false;
        else if (container != null) loadOffers();
    }

    private void loadOffers() {
        if (loading) return;
        setLoading(true);
        repository.getMyOffers(new OffersRepository.Result<List<Offer>>() {
            @Override public void onSuccess(List<Offer> data) {
                if (!isAdded()) return;
                offers.clear();
                offers.addAll(data);
                setLoading(false);
                renderOffers();
            }
            @Override public void onError(String message) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                renderOffers();
            }
        });
    }

    private void renderOffers() {
        if (container == null) return;
        container.removeAllViews();
        int count = 0;
        for (Offer offer : offers) {
            if (!selectedRole.equals(offer.getRole())) continue;
            container.addView(createCard(offer));
            count++;
        }
        emptyView.setVisibility(count == 0 && !loading ? View.VISIBLE : View.GONE);
    }

    private View createCard(Offer offer) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(12);
        card.setLayoutParams(cardParams);
        card.setCardElevation(dp(2));
        card.setRadius(dp(12));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(content);
        addText(content, safe(offer.getTitle(), "Artículo"), 18, true);
        addText(content, "Oferta: " + money(offer.getAmount()), 16, true);
        if (offer.getCounterAmount() != null) {
            addText(content, "Contraoferta: " + money(offer.getCounterAmount()), 16, true);
        }
        addText(content, "Estado: " + statusLabel(offer.getStatus()), 14, false);
        addText(content, "Creada: " + formatDate(offer.getCreatedAt()), 14, false);
        addText(content, expiryText(offer.getExpiresAt()), 14, false);
        if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
            addText(content, "Mensaje: " + offer.getMessage(), 14, false);
        }

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);
        content.addView(actions);
        if ("seller".equals(offer.getRole()) && "pending".equals(offer.getStatus())) {
            addAction(actions, "Aceptar", () -> sellerAction(offer, "accept", null));
            addAction(actions, "Rechazar", () -> sellerAction(offer, "reject", null));
            addAction(actions, "Contraofertar", () -> showCounterDialog(offer));
        } else if ("buyer".equals(offer.getRole()) && "countered".equals(offer.getStatus())) {
            addAction(actions, "Aceptar", () -> counterAction(offer, "accept"));
            addAction(actions, "Rechazar", () -> counterAction(offer, "reject"));
        } else if ("buyer".equals(offer.getRole()) && "pending".equals(offer.getStatus())) {
            addAction(actions, "Cancelar", () -> cancelOffer(offer));
        }
        actions.setVisibility(actions.getChildCount() == 0 ? View.GONE : View.VISIBLE);
        return card;
    }

    private void showCounterDialog(Offer offer) {
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint("Importe de la contraoferta");
        int padding = dp(20);
        layout.setPadding(padding, 0, padding, 0);
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(input);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Contraofertar")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar", null)
                .create();
        dialog.setOnShowListener(ignored ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        String raw = input.getText() == null ? "" : input.getText().toString().trim().replace(',', '.');
                        try {
                            double amount = Double.parseDouble(raw);
                            if (!Double.isFinite(amount) || amount < 0.01 || amount > 999999999d) throw new NumberFormatException();
                            dialog.dismiss();
                            sellerAction(offer, "counter", amount);
                        } catch (NumberFormatException error) {
                            layout.setError("Ingresá un importe entre 0,01 y 999.999.999");
                        }
                    }));
        dialog.show();
    }

    private void sellerAction(Offer offer, String action, Double amount) {
        setLoading(true);
        repository.respond(offer.getId(), action, amount, actionResult());
    }

    private void counterAction(Offer offer, String action) {
        setLoading(true);
        repository.respondToCounter(offer.getId(), action, actionResult());
    }

    private void cancelOffer(Offer offer) {
        setLoading(true);
        repository.cancel(offer.getId(), actionResult());
    }

    private OffersRepository.Result<OfferActionResponse> actionResult() {
        return new OffersRepository.Result<OfferActionResponse>() {
            @Override public void onSuccess(OfferActionResponse data) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Oferta actualizada", Toast.LENGTH_SHORT).show();
                setLoading(false);
                loadOffers();
            }
            @Override public void onError(String message) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
            @Override public void onError(ApiError error) {
                onError(error.getMessage());
                if (error.isOfferNoLongerActionable()) loadOffers();
            }
        };
    }

    private void setLoading(boolean value) {
        loading = value;
        if (progress != null) progress.setVisibility(value ? View.VISIBLE : View.GONE);
        if (refreshButton != null) refreshButton.setEnabled(!value);
    }

    private void addAction(LinearLayout parent, String label, Runnable action) {
        MaterialButton button = new MaterialButton(requireContext(), null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        button.setLayoutParams(params);
        button.setText(label);
        button.setTextSize(11);
        button.setOnClickListener(v -> { if (!loading) action.run(); });
        parent.addView(button);
    }

    private void addText(LinearLayout parent, String value, int size, boolean prominent) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextSize(size);
        if (prominent) text.setTypeface(text.getTypeface(), android.graphics.Typeface.BOLD);
        parent.addView(text);
    }

    private String money(double value) { return MoneyFormat.amount(value); }
    private String safe(String value, String fallback) { return value == null || value.isEmpty() ? fallback : value; }

    private String statusLabel(String status) {
        if (status == null) return "Sin estado";
        switch (status) {
            case "pending": return "Pendiente";
            case "countered": return "Contraofertada";
            case "accepted": return "Aceptada";
            case "rejected": return "Rechazada";
            case "expired": return "Vencida";
            case "cancelled": return "Cancelada";
            default: return status;
        }
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "sin datos";
        try { return OffsetDateTime.parse(raw).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)); }
        catch (RuntimeException ignored) { return raw; }
    }

    private String expiryText(String raw) {
        if (raw == null || raw.isEmpty()) return "Vencimiento: sin datos";
        try {
            Duration remaining = Duration.between(OffsetDateTime.now(), OffsetDateTime.parse(raw));
            if (remaining.isNegative() || remaining.isZero()) return "Vencimiento: plazo cumplido";
            long days = remaining.toDays();
            long hours = remaining.minusDays(days).toHours();
            return "Vence en: " + (days > 0 ? days + " d " : "") + hours + " h";
        } catch (RuntimeException ignored) { return "Vencimiento: " + raw; }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
