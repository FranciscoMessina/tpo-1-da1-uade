package com.da_grupo9.ronda.util;

import com.da_grupo9.ronda.data.model.Operation;

/** Textos compartidos por el historial, el detalle y la calificación de operaciones. */
public final class OperationFormat {
    private OperationFormat() {}

    public static String date(String iso) {
        return DateTimeFormat.mediumDate(iso);
    }

    public static String amount(double amount) {
        return MoneyFormat.amount(amount);
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
