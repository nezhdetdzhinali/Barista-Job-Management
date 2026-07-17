package be.hogent.baristajob2026.dto.response;

import java.time.LocalDate;
import java.util.List;

// voor opleidingsscherm
public record OpleidingDetailDTO(
        Long id,
        String titel,
        String beschrijving,
        LocalDate datum,
        Integer duurInUur,
        Integer maxDeelnemers,
        Integer aantalIngeschreven,
        Long vestigingId,
        String vestigingNaam,
        Boolean alReedsIngeschreven, // enkel relevant voor ingelogde barista, anders altijd false
        List<String> deelnemerNamen  // enkel getoond aan admins in de view (sec:authorize)
) {
}
