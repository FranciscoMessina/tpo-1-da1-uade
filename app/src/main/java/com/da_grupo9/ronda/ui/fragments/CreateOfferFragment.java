package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.OfferActionResponse;
import com.da_grupo9.ronda.data.repository.OffersRepository;
import com.da_grupo9.ronda.util.MoneyFormat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Punto de entrada al flujo de creación de una oferta. */
@AndroidEntryPoint
public class CreateOfferFragment extends Fragment {
    @Inject OffersRepository offersRepository;

    private TextInputLayout priceLayout;
    private TextInputLayout messageLayout;
    private TextInputEditText inputPrice;
    private TextInputEditText inputMessage;
    private MaterialButton buttonSend;
    private CircularProgressIndicator progress;
    private String publicationId;
    private boolean sending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        publicationId = args == null ? "" : args.getString("publicationId", "");
        String titulo = args == null ? "" : args.getString("publicationTitle", "");
        String vendedor = args == null ? "" : args.getString("sellerName", "");
        double precio = args == null ? 0d : args.getDouble("publicationPrice", 0d);

        ((TextView) view.findViewById(R.id.textOfferPublication)).setText(titulo);
        ((TextView) view.findViewById(R.id.textOfferPublishedPrice))
                .setText("Precio publicado: " + MoneyFormat.amount(precio));
        ((TextView) view.findViewById(R.id.textOfferSeller))
                .setText(vendedor.isEmpty() ? "" : "Vendedor: " + vendedor);
        priceLayout = view.findViewById(R.id.inputOfferPriceLayout);
        messageLayout = view.findViewById(R.id.inputOfferMessageLayout);
        inputPrice = view.findViewById(R.id.inputOfferPrice);
        inputMessage = view.findViewById(R.id.inputOfferMessage);
        buttonSend = view.findViewById(R.id.buttonSendOffer);
        progress = view.findViewById(R.id.progressSendOffer);
        inputPrice.setText(MoneyFormat.editableAmount(precio));
        buttonSend.setOnClickListener(v -> enviarOferta());
        view.findViewById(R.id.buttonOfferBack).setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack());
    }

    private void enviarOferta() {
        if (sending) return;
        priceLayout.setError(null);
        messageLayout.setError(null);
        String precioTexto = inputPrice.getText() == null
                ? "" : inputPrice.getText().toString().trim().replace(',', '.');
        double monto;
        try {
            monto = Double.parseDouble(precioTexto);
        } catch (NumberFormatException error) {
            priceLayout.setError("Ingresá un precio válido");
            return;
        }
        if (!Double.isFinite(monto) || monto < 0.01 || monto > 999999999d) {
            priceLayout.setError("El importe debe estar entre 0,01 y 999.999.999");
            return;
        }
        if (publicationId.isEmpty()) {
            Toast.makeText(requireContext(), "No se pudo identificar la publicación",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String mensaje = inputMessage.getText() == null
                ? "" : inputMessage.getText().toString().trim();
        if (mensaje.length() > 500) {
            messageLayout.setError("El mensaje no puede superar los 500 caracteres");
            return;
        }
        if (!offersRepository.isOnline()) {
            Toast.makeText(requireContext(), "Se necesita conexión a internet para enviar una oferta",
                    Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        offersRepository.createOffer(publicationId, monto, mensaje,
                new OffersRepository.Result<OfferActionResponse>() {
                    @Override public void onSuccess(OfferActionResponse data) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Oferta enviada", Toast.LENGTH_SHORT).show();
                        Bundle args = new Bundle();
                        args.putString("selectedRole", "buyer");
                        NavOptions options = new NavOptions.Builder()
                                .setPopUpTo(R.id.createOfferFragment, true)
                                .build();
                        Navigation.findNavController(requireView()).navigate(
                                R.id.offersFragment, args, options);
                    }

                    @Override public void onError(String mensajeError) {
                        if (!isAdded()) return;
                        setLoading(false);
                        Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        sending = loading;
        inputPrice.setEnabled(!loading);
        inputMessage.setEnabled(!loading);
        buttonSend.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

}
