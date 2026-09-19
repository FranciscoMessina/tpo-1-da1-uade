package com.da_grupo9.ronda.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.model.Publicacion;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestionFragment extends Fragment {

    @Inject PublicacionRepository publicacionRepository;

    private TextInputLayout inputLayout;
    private TextInputEditText inputQuestion;
    private MaterialButton buttonSend;
    private CircularProgressIndicator progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String publicacionId = getArguments() != null
                ? getArguments().getString("publicacionId", "") : "";
        String titulo = getArguments() != null
                ? getArguments().getString("publicacionTitulo", "") : "";
        String vendedor = getArguments() != null
                ? getArguments().getString("vendedorNombre", "") : "";

        inputLayout = view.findViewById(R.id.inputQuestionLayout);
        inputQuestion = view.findViewById(R.id.inputQuestion);
        buttonSend = view.findViewById(R.id.buttonSendQuestion);
        progress = view.findViewById(R.id.progressSendQuestion);
        TextView context = view.findViewById(R.id.textQuestionContext);

        StringBuilder description = new StringBuilder();
        if (!titulo.isEmpty()) description.append(titulo);
        if (!vendedor.isEmpty()) {
            if (description.length() > 0) description.append("\n");
            description.append("Vendedor: ").append(vendedor);
        }
        context.setText(description);
        context.setVisibility(description.length() == 0 ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.buttonQuestionBack).setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack());
        buttonSend.setOnClickListener(v -> enviarPregunta(publicacionId));

        inputQuestion.requestFocus();
    }

    private void enviarPregunta(String publicacionId) {
        String texto = inputQuestion.getText() == null
                ? "" : inputQuestion.getText().toString().trim();
        if (texto.isEmpty()) {
            inputLayout.setError("Escribí una pregunta antes de enviarla");
            return;
        }
        if (publicacionId.isEmpty()) {
            Toast.makeText(requireContext(), "No se pudo identificar la publicación", Toast.LENGTH_LONG).show();
            return;
        }

        inputLayout.setError(null);
        setLoading(true);
        publicacionRepository.crearPregunta(publicacionId, texto,
                new PublicacionRepository.Resultado<Publicacion.Question>() {
                    @Override
                    public void onSuccess(Publicacion.Question data) {
                        if (!isAdded()) return;
                        ocultarTeclado();
                        Toast.makeText(requireContext(), "Pregunta enviada", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (!isAdded()) return;
                        setLoading(false);
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        inputQuestion.setEnabled(!loading);
        buttonSend.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputQuestion.getWindowToken(), 0);
    }
}
