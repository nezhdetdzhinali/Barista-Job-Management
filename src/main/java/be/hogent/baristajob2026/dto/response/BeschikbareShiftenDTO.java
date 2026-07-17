package be.hogent.baristajob2026.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// voor REST - aantal beschikbare shifts voor een barista
@JsonPropertyOrder({"barista_id", "aantal_beschikbare_shifts"})
public record BeschikbareShiftenDTO(
        @JsonProperty("barista_id")
        Long baristaId,
        @JsonProperty("aantal_beschikbare_shifts")
        int aantalBeschikbareShifts
) {
}
