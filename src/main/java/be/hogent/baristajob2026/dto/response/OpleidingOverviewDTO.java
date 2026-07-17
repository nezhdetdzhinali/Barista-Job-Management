package be.hogent.baristajob2026.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// voor detail pagina van een barista "voltooide/geplande opleidingen"
// voor REST - opleidingen per vestiging
@JsonPropertyOrder({"opleiding_id", "titel"})
public record OpleidingOverviewDTO(
        @JsonProperty("opleiding_id")
        Long id, // wordt gebruikt als vestigingId in de API call
        String titel) {
}
