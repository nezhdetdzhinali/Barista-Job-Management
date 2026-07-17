package be.hogent.baristajob2026.init;

import be.hogent.baristajob2026.dto.request.OpleidingInputDTO;
import lombok.Builder;

import java.time.LocalDate;

import static be.hogent.baristajob2026.init.InitOpleiding.*;

@Builder(toBuilder = true)
public class BuilderOpleidingDTO {

    @Builder.Default
    private Long id = OK_ID;

    @Builder.Default
    private String titel = OK_TITEL;

    @Builder.Default
    private String beschrijving = OK_BESCHRIJVING;

    @Builder.Default
    private LocalDate datum = OK_DATUM;

    @Builder.Default
    private Integer duurInUur = OK_DUUR_IN_UUR;

    @Builder.Default
    private Integer maxDeelnemers = OK_MAX_DEELNEMERS;

    @Builder.Default
    private Long vestigingId = OK_VESTIGING_ID;

    public OpleidingInputDTO toDTO() {
        return new OpleidingInputDTO(id, titel, beschrijving, datum, duurInUur, maxDeelnemers, vestigingId);
    }
}
