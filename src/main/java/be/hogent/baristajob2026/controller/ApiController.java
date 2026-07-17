package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.response.BaristaOverviewDTO;
import be.hogent.baristajob2026.dto.response.BeschikbareShiftenDTO;
import be.hogent.baristajob2026.dto.response.OpleidingOverviewDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final BaristaService baristaService;
    private final ShiftService shiftService;
    private final OpleidingService opleidingService;

    // GET /api/baristas/stad/Gent
    @GetMapping("/baristas/stad/{stad}")
    public List<BaristaOverviewDTO> getBaristasByStad(@PathVariable String stad) {
        return baristaService.findByStad(stad);
    }

    // GET /api/baristas/1/shifts/beschikbaar
    @GetMapping("/baristas/{id}/shifts/beschikbaar")
    public BeschikbareShiftenDTO getAantalBeschikbareShifts(@PathVariable Long id) {
        return new BeschikbareShiftenDTO(id, shiftService.countBeschikbaarVoorBarista(id));
    }

    // GET /api/opleidingen/vestiging/1
    @GetMapping("/opleidingen/vestiging/{vestigingId}")
    public List<OpleidingOverviewDTO> getOpleidingenPerVestiging(@PathVariable Long vestigingId) {
        return opleidingService.findByVestigingId(vestigingId);
    }
}