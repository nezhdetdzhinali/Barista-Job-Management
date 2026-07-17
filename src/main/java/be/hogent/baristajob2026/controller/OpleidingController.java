package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.FilterOpleidingDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.VestigingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import be.hogent.baristajob2026.dto.request.OpleidingInputDTO;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@RequestMapping("/opleiding")
public class OpleidingController {
    private final OpleidingService opleidingService;
    private final VestigingService vestigingService; // nodig voor vestiging-filter dropdown
    private final BaristaService baristaService;      // nodig om ingelogde barista-id op te zoeken

    // toont het opleidingsscherm (zichtbaar voor iedereen)
    // RBAC op deelnemerslijst/inschrijfknop gebeurt in de view (sec:authorize)
    // we passen een object van authentication omdat we moeten weten of de huidige gebruiker een barista is (voor alReedsIngeschreven + inschrijfknop)
    // anonieme gebruikers en admins krijgen null, zij kunnen zich niet inschrijven
    @GetMapping
    public String showOverview(@ModelAttribute("filterOpleidingDTO") FilterOpleidingDTO filterOpleidingDTO,
                               Model model, Authentication authentication) {
        Long baristaId = huidigeBaristaId(authentication);
        addSharedAttributes(model, baristaId);
        model.addAttribute("opleidingen", opleidingService.findFilteredOpleidingen(filterOpleidingDTO, baristaId));
        return "opleiding-overview";
    }

    @PostMapping
    public String processFilter(FilterOpleidingDTO filterOpleidingDTO, Model model, Authentication authentication) {
        Long baristaId = huidigeBaristaId(authentication);
        addSharedAttributes(model, baristaId);
        model.addAttribute("opleidingen", opleidingService.findFilteredOpleidingen(filterOpleidingDTO, baristaId));
        return "opleiding-overview";
    }
    // inschrijven voor een opleiding
    @PostMapping("/{opleidingId}/inschrijven")
    public String processInschrijven(@PathVariable Long opleidingId, Authentication authentication, RedirectAttributes ra) {
        Long baristaId = baristaService.findIdByEmail(authentication.getName());
        String foutmelding = opleidingService.inschrijven(opleidingId, baristaId);
        if (foutmelding != null) {
            ra.addFlashAttribute("foutmelding", foutmelding);
        }
        return "redirect:/opleiding";
    }

    @GetMapping("/nieuw")
    public String showNieuwForm(@ModelAttribute("opleidingInputDTO") OpleidingInputDTO opleidingInputDTO, Model model) {
        addFormAttributes(model);
        return "opleiding-form";
    }

    @PostMapping("/nieuw")
    public String processNieuwForm(@Valid OpleidingInputDTO opleidingInputDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormAttributes(model);
            return "opleiding-form";
        }
        opleidingService.createOpleiding(opleidingInputDTO);
        return "redirect:/opleiding";
    }

    @GetMapping("/{id}/bewerken")
    public String showBewerkenForm(@PathVariable Long id, Model model) {
        model.addAttribute("opleidingInputDTO", opleidingService.findInputById(id));
        addFormAttributes(model);
        return "opleiding-form";
    }

    @PostMapping("/{id}/bewerken")
    public String processBewerkenForm(@PathVariable Long id, @Valid OpleidingInputDTO opleidingInputDTO,
                                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormAttributes(model);
            return "opleiding-form";
        }
        opleidingService.updateOpleiding(id, opleidingInputDTO);
        return "redirect:/opleiding";
    }
    // gemeenschappelijke model-attributen voor GET en POST
    private void addSharedAttributes(Model model, Long baristaId) {
        model.addAttribute("vestigingen", vestigingService.findAll());
        model.addAttribute("baristaId", baristaId); // nodig in de view om de inschrijf-URL op te bouwen
    }

    // enkel de vestigingen-dropdown, los van addSharedAttributes (die is specifiek voor de
    // overview/filter-pagina's en heeft baristaId nodig, wat hier niet relevant is)
    private void addFormAttributes(Model model) {
        model.addAttribute("vestigingen", vestigingService.findAll());
    }

    // enkel ingelogde baristas hebben hier iets aan (voor alReedsIngeschreven + de inschrijfknop)
    // anoniem en admin krijgen null, zij kunnen zich  niet inschrijven
    private Long huidigeBaristaId(Authentication authentication) {
        boolean isBarista = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BARISTA"));
        return isBarista ? baristaService.findIdByEmail(authentication.getName()) : null;
    }
}
