package be.hogent.baristajob2026.utils;

import java.time.format.DateTimeFormatter;

public final class DateFormats {
    private DateFormats() {

    }
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
}
