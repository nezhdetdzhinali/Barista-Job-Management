package be.hogent.baristajob2026.dto.response;
import java.util.List;

// voor detail pagina vestiging met baristas, shifts en opleidingen
public record VestigingDetailDTO(
        Long id,
        String naam,
        String stad,
        int aantalZitplaatsen,
        List<BaristaOverviewDTO> baristas,
        List<ShiftDetailDTO> shifts,
        List<OpleidingOverviewDTO> opleidingen
) {
}
