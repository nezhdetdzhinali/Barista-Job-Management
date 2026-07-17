package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.model.ShiftRol;
import be.hogent.baristajob2026.validator.GeldigeShiftTijden;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
// shift toevoegen of wijzigen form input
@GeldigeShiftTijden
public record ShiftInputDTO(
        Long id,

        @NotNull(message = "{shift.datum.required}")
        // datepicker heeft dit patroon nodig, anders wordt datum locale-afhankelijk geformatteerd
        // en toont <input type="date"> het veld als leeg
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate datum,

        @NotNull(message = "{shift.startuur.required}")
        LocalTime startUur,

        @NotNull(message = "{shift.einduur.required}")
        LocalTime eindUur,

        @NotNull(message = "{shift.rol.required}")
        ShiftRol rol,

        // maximaal aantal barista's per shift (X), wordt ingesteld bij het aanmaken van de shift
        @Min(value = 1, message = "{shift.maxbaristas.min}")
        Integer maxBaristas,

        // voor toevoegen/wijzigen van shift is de id van de vestiging nodig want een shift heeft een vestiging
        @NotNull(message = "{shift.vestiging.required}")
        Long vestigingId
) {
}
