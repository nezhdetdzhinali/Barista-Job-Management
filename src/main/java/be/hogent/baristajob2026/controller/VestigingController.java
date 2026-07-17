package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import be.hogent.baristajob2026.dto.response.VestigingDetailDTO;
import be.hogent.baristajob2026.service.VestigingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vestiging")
public class VestigingController {
    private final VestigingService vestigingService;

    // detailpagina - zichtbaar voor iedereen
    // RBAC op de baristagegevens binnen de pagina gebeurt via sec:authorize in de view
    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        VestigingDetailDTO vestiging = vestigingService.findDetailById(id);
        model.addAttribute("vestiging", vestiging);
        return "vestiging-detail";
    }

    // toont het lege formulier voor een nieuwe vestiging (Admin)
    @GetMapping("/nieuw")
    public String showNieuwForm(@ModelAttribute("vestigingInputDTO") VestigingInputDTO vestigingInputDTO) {
        return "vestiging-form";
    }

    @PostMapping("/nieuw")
    public String processNieuwForm(@Valid VestigingInputDTO vestigingInputDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "vestiging-form";
        }
        vestigingService.createVestiging(vestigingInputDTO);
        return "redirect:/overview";
    }

    // toont het formulier voor een bestaande vestiging, vooraf ingevuld (Admin)
    @GetMapping("/{id}/bewerken")
    public String showBewerkenForm(@PathVariable Long id, Model model) {
        model.addAttribute("vestigingInputDTO", vestigingService.findInputById(id));
        return "vestiging-form";
    }

    @PostMapping("/{id}/bewerken")
    public String processBewerkenForm(@PathVariable Long id,
                                      @Valid VestigingInputDTO vestigingInputDTO,
                                      BindingResult result) {
        if (result.hasErrors()) {
            return "vestiging-form";
        }
        vestigingService.updateVestiging(id, vestigingInputDTO);
        return "redirect:/vestiging/" + id;
    }
}
