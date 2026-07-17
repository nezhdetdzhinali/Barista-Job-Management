package be.hogent.baristajob2026.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// vestiging toevoegen/wijzigen form input
public record VestigingInputDTO(
        Long id,

        @NotBlank(message = "{vestiging.naam.required}")
        String naam,

        @NotBlank(message = "{vestiging.stad.required}")
        String stad,

        @NotNull(message = "{vestiging.zitplaatsen.required}")
        @Min(value = 1, message = "{vestiging.zitplaatsen.min}")
        Integer aantalZitplaatsen
) {
}
