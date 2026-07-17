package be.hogent.baristajob2026.init;

import be.hogent.baristajob2026.dto.request.ShiftInputDTO;
import be.hogent.baristajob2026.model.ShiftRol;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

import static be.hogent.baristajob2026.init.InitShift.*;

@Builder(toBuilder = true)
public class BuilderShiftDTO {
    @Builder.Default
    private Long id = OK_ID;

    @Builder.Default
    private LocalDate datum = OK_DATUM;

    @Builder.Default
    private LocalTime startUur = OK_START_UUR;

    @Builder.Default
    private LocalTime eindUur = OK_EIND_UUR;

    @Builder.Default
    private ShiftRol rol = OK_ROL;

    @Builder.Default
    private Integer maxBaristas = OK_MAX_BARISTAS;

    @Builder.Default
    private Long vestigingId = OK_VESTIGING_ID;

    public ShiftInputDTO toDTO() {
        return new ShiftInputDTO(id, datum, startUur, eindUur, rol, maxBaristas, vestigingId);
    }
}
