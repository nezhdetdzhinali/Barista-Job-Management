package be.hogent.baristajob2026.dto.response;

import java.time.LocalDate;
import java.util.List;

public record BaristaDetailDTO(
        Long id,
        String voornaam,
        String achternaam,
        String email,
        LocalDate geboortedatum,
        String studentenkaartNummer,
        boolean actief,
        String vestigingNaam,
        List<ShiftDetailDTO> toekomstigeShifts,
        List<ShiftDetailDTO> afgelopenShifts,
        List<OpleidingOverviewDTO> geplandeOpleidingen,
        List<OpleidingOverviewDTO> voltooideOpleidingen
) {
}
