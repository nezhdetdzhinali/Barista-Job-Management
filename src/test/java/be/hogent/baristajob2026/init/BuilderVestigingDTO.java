package be.hogent.baristajob2026.init;

import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import lombok.Builder;

import static be.hogent.baristajob2026.init.InitVestiging.*;

@Builder(toBuilder = true)
public class BuilderVestigingDTO {

    @Builder.Default
    private Long id = OK_ID;

    @Builder.Default
    private String naam = OK_NAAM;

    @Builder.Default
    private String stad = OK_STAD;

    @Builder.Default
    private Integer aantalZitplaatsen = OK_AANTAL_ZITPLAATSEN;

    public VestigingInputDTO toDTO() {
        return new VestigingInputDTO(id, naam, stad, aantalZitplaatsen);
    }
}
