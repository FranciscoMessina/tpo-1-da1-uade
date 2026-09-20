package com.da_grupo9.ronda.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/** Formatos localizados para fechas ISO visibles. */
public final class DateTimeFormat {
    private DateTimeFormat() {}

    public static String mediumDate(String iso) {
        return format(iso, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public static String shortDateTime(String iso) {
        return format(iso, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
    }

    private static String format(String iso, DateTimeFormatter formatter) {
        if (iso == null || iso.isEmpty()) return "sin datos";
        try {
            return OffsetDateTime.parse(iso).format(formatter);
        } catch (RuntimeException ignored) {
            return iso;
        }
    }
}
