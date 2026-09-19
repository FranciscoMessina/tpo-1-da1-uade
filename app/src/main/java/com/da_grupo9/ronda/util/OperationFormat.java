package com.da_grupo9.ronda.util;

import com.da_grupo9.ronda.data.model.Operation;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/** Textos compartidos por el historial, el detalle y la calificación de operaciones. */
public final class OperationFormat {
    private OperationFormat() {}

    public static String date(String iso) {
        if (iso == null || iso.isEmpty()) return "sin datos";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        } catch (RuntimeException ignored) {
            return iso;
        }
    }

    public static String amount(double amount) {
        return "$" + String.format("%,.0f", amount);
    }

    public static String typeLabel(Operation operation) {
        return operation.isPurchase() ? "Compra" : "Venta";
    }

    public static String counterpartyLabel(Operation operation) {
        String name = operation.getCounterpartyName();
        if (name != null && !name.trim().isEmpty()) return name;
        return operation.isPurchase() ? "Vendedor" : "Comprador";
    }

    public static String counterpartyRole(Operation operation) {
        return operation.isPurchase() ? "Vendedor" : "Comprador";
    }

    public static String stars(int rating) {
        int clamped = Math.max(0, Math.min(5, rating));
        return "★".repeat(clamped) + "☆".repeat(5 - clamped);
    }

    public static String ratingStatus(Operation operation) {
        Integer rating = operation.getMyRating();
        if (rating != null) return "Tu calificación: " + stars(rating);
        return operation.canRate() ? "Pendiente de calificar" : "Plazo de calificación vencido";
    }
}
