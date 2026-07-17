package be.hogent.baristajob2026.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
// voor barista detail page (toekomsitge en afgelopen shifts), en vestiging detail page
// wordt ook gebruikt voor admin edit/delete shift
public record ShiftDetailDTO(
        Long id,
        LocalDate datum,
        LocalTime startUur,
        LocalTime eindUur
) {
}
