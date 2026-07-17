package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.FilterBaristaDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.VestigingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/overview")
public class OverviewController {
    private final VestigingService vestigingService;
    private final BaristaService baristaService;
    private final OpleidingService opleidingService;

    // toont het initiele scherm (/overview) met alle data en ingeladen dropdown opties
    // @ModelAttribute("filterBaristaDTO") FilterBaristaDTO filterBaristaDTO zorgt ervoor dat
    // er een lege FilterBaristaDTO wordt aangemaakt en toegevoegd aan het model, zodat de form in de view correct kan worden weergegeven
    @GetMapping
    public String showOverview(@ModelAttribute("filterBaristaDTO") FilterBaristaDTO filterBaristaDTO, Model model) {
        addSharedAttributes(model);
        model.addAttribute("baristas", baristaService.findAllOverview());
        return "overview";
    }

    @PostMapping
    public String processOverviewFilter(FilterBaristaDTO filterBaristaDTO, Model model) {
        addSharedAttributes(model);
        // gefilterde lijst met baristas op basis van de filter criteria
        model.addAttribute("baristas", baristaService.findFilteredBaristas(filterBaristaDTO));
        return "overview";
    }

    // gemeenschappelijke model-attributen voor GET en POST
    // voordeel is dat als er een vierde attribute bijkomt, we die maar op 1 plaats moeten toevoegen ipv in beide methoden
    private void addSharedAttributes(Model model) {
        model.addAttribute("vestigingen", vestigingService.findAll());
        model.addAttribute("stedenList", vestigingService.getAllSteden());
        model.addAttribute("opleidingenList", opleidingService.findAll());
    }
}
