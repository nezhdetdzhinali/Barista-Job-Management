package be.hogent.baristajob2026.init;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import lombok.Builder;

import java.time.LocalDate;

import static be.hogent.baristajob2026.init.InitBarista.*;

@Builder(toBuilder = true)
public class BuilderBaristaDTO {

    @Builder.Default
    private Long id = OK_ID;

    @Builder.Default
    private String voornaam = OK_VOORNAAM;

    @Builder.Default
    private String achternaam = OK_ACHTERNAAM;

    @Builder.Default
    private String email = OK_EMAIL;

    @Builder.Default
    private LocalDate geboortedatum = OK_GEBOORTEDATUM;

    @Builder.Default
    private String studentenkaartNummer = OK_STUDENTENKAARTNUMMER;

    @Builder.Default
    private Boolean actief = OK_ACTIEF;

    @Builder.Default
    private Long vestigingId = OK_VESTIGING_ID;

    // @Builder werkt enkel op klassen, niet op records, dus we converteren nog naar de record
    public BaristaInputDTO toDTO() {
        return new BaristaInputDTO(id, voornaam, achternaam, email, geboortedatum, studentenkaartNummer, actief, vestigingId);
    }
}