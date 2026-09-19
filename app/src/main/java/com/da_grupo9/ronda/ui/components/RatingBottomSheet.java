package com.da_grupo9.ronda.ui.components;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.da_grupo9.ronda.R;
import com.da_grupo9.ronda.data.repository.RepositoryResult;
import com.da_grupo9.ronda.data.repository.ProfileRepository;
import com.da_grupo9.ronda.data.repository.PublicacionRepository;
import com.da_grupo9.ronda.util.ApiError;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Permite calificar a la contraparte de una operación. Se muestra con el childFragmentManager del
 * fragment que lo abre y le avisa el resultado con {@link #REQUEST_KEY}.
 */
@AndroidEntryPoint
public class RatingBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "RatingBottomSheet";
    public static final String REQUEST_KEY = "operation_rating";
    public static final String RESULT_OPERATION_ID = "operationId";
    public static final String RESULT_RATING = "rating";
    public static final String RESULT_REFRESH_ONLY = "refreshOnly";

    private static final String ARG_OPERATION_ID = "operationId";
    private static final String ARG_COUNTERPARTY = "counterparty";

    @Inject ProfileRepository profileRepository;

    private RatingBar ratingBar;
    private TextInputEditText inputComment;
    private TextView textError;
    private MaterialButton buttonSend;

    public static RatingBottomSheet newInstance(String operationId, String counterparty) {
        RatingBottomSheet sheet = new RatingBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_OPERATION_ID, operationId);
        args.putString(ARG_COUNTERPARTY, counterparty);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String operationId = args.getString(ARG_OPERATION_ID, "");
        String counterparty = args.getString(ARG_COUNTERPARTY, "");

        ratingBar = view.findViewById(R.id.ratingBar);
        inputComment = view.findViewById(R.id.inputRatingComment);
        textError = view.findViewById(R.id.textRatingError);
        buttonSend = view.findViewById(R.id.buttonSendRating);
        ((TextView) view.findViewById(R.id.textRatingCounterparty))
                .setText("¿Cómo fue tu experiencia con " + counterparty + "?");

        buttonSend.setOnClickListener(v -> enviar(operationId));
    }

    private void enviar(String operationId) {
        int rating = Math.round(ratingBar.getRating());
        if (rating < 1) {
            mostrarError("Elegí de 1 a 5 estrellas");
            return;
        }
        String comment = inputComment.getText() == null ? "" : inputComment.getText().toString().trim();

        setLoading(true);
        profileRepository.createReview(operationId, rating, comment.isEmpty() ? null : comment,
                new RepositoryResult<Void>() {
                    @Override public void onSuccess(Void data) {
                        if (!isAdded()) return;
                        Bundle result = new Bundle();
                        result.putString(RESULT_OPERATION_ID, operationId);
                        result.putInt(RESULT_RATING, rating);
                        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                        dismissAllowingStateLoss();
                    }

                    @Override public void onError(String mensaje) {
                        if (!isAdded()) return;
                        setLoading(false);
                        mostrarError(mensaje);
                    }

                    @Override public void onError(ApiError error) {
                        if (!error.isReviewAlreadySubmitted()) {
                            onError(error.getMessage());
                            return;
                        }
                        if (!isAdded()) return;
                        Bundle result = new Bundle();
                        result.putString(RESULT_OPERATION_ID, operationId);
                        result.putBoolean(RESULT_REFRESH_ONLY, true);
                        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                        dismissAllowingStateLoss();
                    }
                });
    }

    private void setLoading(boolean loading) {
        setCancelable(!loading);
        buttonSend.setEnabled(!loading);
        ratingBar.setIsIndicator(loading);
        inputComment.setEnabled(!loading);
        if (loading) textError.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        textError.setText(mensaje);
        textError.setVisibility(View.VISIBLE);
    }
}
