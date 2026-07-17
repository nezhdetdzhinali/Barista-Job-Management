package be.hogent.baristajob2026.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

// voor "mijn shifts" en "beschikbare shifts" op de shiftpagina van een barista
public record ShiftOverviewDTO(
        Long id,
        LocalDate datum,
        LocalTime startUur,
        LocalTime eindUur,
        String rol,
        String vestigingNaam,
        int aantalIngeschreven,
        int maxBaristas
) {
}
