package com.da_grupo9.ronda.util;

import org.junit.Test;
import java.util.Locale;
import static org.junit.Assert.assertEquals;

public class DateTimeFormatTest {
    @Test public void missingDatesHaveFallback() {
        assertEquals("sin datos", DateTimeFormat.mediumDate(null));
        assertEquals("sin datos", DateTimeFormat.mediumDate(""));
        assertEquals("sin datos", DateTimeFormat.shortDateTime(null));
        assertEquals("sin datos", DateTimeFormat.shortDateTime(""));
    }

    @Test public void invalidDatesRemainUnchanged() {
        for (String input : new String[]{"no es una fecha", " ", "2026-09-20"}) {
            assertEquals(input, DateTimeFormat.mediumDate(input));
            assertEquals(input, DateTimeFormat.shortDateTime(input));
        }
    }

    @Test public void validDatesKeepLocalizedStylesAndOriginalOffset() {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.UK);
            String iso = "2026-09-20T23:45:00-03:00";
            assertEquals("20 Sept 2026", DateTimeFormat.mediumDate(iso));
            assertEquals("20/09/2026, 23:45", DateTimeFormat.shortDateTime(iso));
            assertEquals(DateTimeFormat.mediumDate(iso), OperationFormat.date(iso));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }
}
