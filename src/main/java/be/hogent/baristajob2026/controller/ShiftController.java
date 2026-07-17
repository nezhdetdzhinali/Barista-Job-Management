package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.ShiftInputDTO;
import be.hogent.baristajob2026.model.ShiftRol;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.ShiftService;
import be.hogent.baristajob2026.service.VestigingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shift")
public class ShiftController {
    private final ShiftService shiftService;
    private final VestigingService vestigingService; // nodig voor vestiging-select in shift form
    private final BaristaService baristaService; // nodig om ingelogde barista-id op te zoeken

    // form voor het aanmaken/wijzigen van een nieuwe shift
    @GetMapping("/nieuw")
    public String showNieuwForm(@ModelAttribute("shiftInputDTO") ShiftInputDTO shiftInputDTO, Model model) {
        addFormAttributes(model);
        return "shift-form";
    }

    // verwerk de form submission voor het aanmaken van een nieuwe shift
    @PostMapping("/nieuw")
    public String processNieuwForm(@Valid ShiftInputDTO shiftInputDTO, BindingResult result, Model model) {

        if (result.hasErrors()) {
            addFormAttributes(model);
            return "shift-form";
        }
        shiftService.createShift(shiftInputDTO);
        return "redirect:/vestiging/" + shiftInputDTO.vestigingId();
    }

    // reageer op GET request voor het bewerken van een bestaande shift
    @GetMapping("/{id}/bewerken")
    public String showBewerkenForm(@PathVariable Long id, Model model) {
        model.addAttribute("shiftInputDTO", shiftService.findInputById(id)); // bestaande shift data toevoegen
        addFormAttributes(model);
        return "shift-form";
    }

    // reageer op POST request voor het bewerken van een bestaande shift
    @PostMapping("/{id}/bewerken")
    public String processBewerkenForm(@PathVariable Long id, @Valid ShiftInputDTO shiftInputDTO,
                                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormAttributes(model);
            return "shift-form";
        }
        shiftService.updateShift(id, shiftInputDTO);
        return "redirect:/vestiging/" + shiftInputDTO.vestigingId();
    }
    // reageer op POST request voor het verwijderen van een bestaande shift
    @PostMapping("/{id}/verwijderen")
    public String processVerwijderen(@PathVariable Long id) {
        Long vestigingId = shiftService.deleteShift(id);
        return vestigingId != null ? "redirect:/vestiging/" + vestigingId : "redirect:/overview";
    }

    // inschrijven voor een shift
    @PostMapping("/{shiftId}/inschrijven")
    public String processInschrijven(@PathVariable Long shiftId, Authentication authentication, RedirectAttributes ra) {
        Long baristaId = baristaService.findIdByEmail(authentication.getName());
        String foutmelding = shiftService.inschrijven(shiftId, baristaId);
        if (foutmelding != null) {
            ra.addFlashAttribute("foutmelding", foutmelding);
        }
        return "redirect:/barista/" + baristaId + "/shifts";
    }

    private void addFormAttributes(Model model) {
        model.addAttribute("vestigingen", vestigingService.findAll());
        model.addAttribute("shiftRollen", ShiftRol.values());
    }
}
