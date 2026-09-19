package com.da_grupo9.ronda.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formato compartido para importes visibles y editables. */
public final class MoneyFormat {
    private MoneyFormat() {}

    public static String amount(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "$" + formatter.format(amount);
    }

    public static String editableAmount(double amount) {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }
}
