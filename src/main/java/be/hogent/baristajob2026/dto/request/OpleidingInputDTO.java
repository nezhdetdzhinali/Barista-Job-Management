package be.hogent.baristajob2026.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// opleiding toevoegen/wijzigen form input
public record OpleidingInputDTO(
        Long id,

        @NotBlank(message = "{opleiding.titel.required}")
        String titel,

        @NotBlank(message = "{opleiding.beschrijving.required}")
        String beschrijving,

        @NotNull(message = "{opleiding.datum.required}")
        // datepicker heeft dit patroon nodig, anders wordt datum locale-afhankelijk geformatteerd
        // en toont <input type="date"> het veld als leeg
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate datum,

        @Min(value = 1, message = "{opleiding.duur.positive}")
        Integer duurInUur,

        @Min(value = 1, message = "{opleiding.maxdeelnemers.positive}")
        Integer maxDeelnemers,

        @NotNull(message = "{opleiding.vestiging.required}")
        Long vestigingId
) {
}
