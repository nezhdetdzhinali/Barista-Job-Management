package be.hogent.baristajob2026.dto.response;

import be.hogent.baristajob2026.utils.GeboortedatumDeserializer;
import be.hogent.baristajob2026.utils.GeboortedatumSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.List;

// voor barista overview sectie
// wordt ook voor REST gebruikt - baristas in X stad
@JsonPropertyOrder({"barista_id", "voornaam", "achternaam", "stad", "actief", "geboortedatum", "aantal_shifts", "opleidingen"})
public record BaristaOverviewDTO(
        @JsonProperty("barista_id")
        Long id,
        String voornaam,
        String achternaam,
        String stad,
        boolean actief,
        @JsonSerialize(using = GeboortedatumSerializer.class)
        @JsonDeserialize(using = GeboortedatumDeserializer.class)
        LocalDate geboortedatum,
        @JsonProperty("aantal_shifts")
        int aantalShifts,
        List<OpleidingOverviewDTO> opleidingen
) {}