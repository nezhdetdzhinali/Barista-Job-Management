package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.dto.response.BaristaDetailDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
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
@RequestMapping("/barista")
public class BaristaController {
    private final BaristaService baristaService;
    private final VestigingService vestigingService; // nodig voor barista form bewerkingen
    private final ShiftService shiftService; // nodig voor shifts beheren/inschrijven
    private final OpleidingService opleidingService; // nodig voor opleiding inschrijven

    // detailpagina - zichtbaar voor admins en de barista zelf
    // controller haalt enkel de nodige gegevens uit de Authentication (rol, email)
    // en geeft die door aan de service; de service beslist of toegang toegestaan is
    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        BaristaDetailDTO barista = baristaService.findDetailById(id, authentication.getName(), isAdmin);

        model.addAttribute("barista", barista);
        // om in de view de "shifts beheren" knop enkel te tonen aan de betreffende barista
        model.addAttribute("isEigenProfiel", authentication.getName().equals(barista.email()));

        return "barista-detail";
    }


    // toont het lege formulier voor een nieuwe barista (Admin)
    // @ModelAttribute("baristaInputDTO") BaristaInputDTO zorgt ervoor dat er een lege
    // BaristaInputDTO wordt aangemaakt en toegevoegd aan het model , zodat de form in de view correct kan worden weergegeven
    @GetMapping("/nieuw")
    public String showNieuwForm(@ModelAttribute("baristaInputDTO") BaristaInputDTO baristaInputDTO, Model model) {
        addFormAttributes(model);
        return "barista-form";
    }

    // via de Valid annotation wordt de validatie in de DTO klasse toegepast
    // (jakarta annotaties + BaristaUniekValidator), alles dat niet voldoet aan
    // de validatie gaat naar de bindingresult
    // je moet altijd @Valid -> bindingresult volgorde respecteren
    @PostMapping("/nieuw")
    public String processNieuwForm(@Valid BaristaInputDTO baristaInputDTO,
                                   BindingResult result, Model model) {

        // als er fouten zijn keer je terug naar hetzelfde scherm
        if (result.hasErrors()) {
            addFormAttributes(model);
            return "barista-form";
        }

        // geen fouten, mag doorgaan
        baristaService.createBarista(baristaInputDTO);
        return "redirect:/overview";
    }

    // toont het formulier voor een bestaande barista, vooraf ingevuld (Admin)
    @GetMapping("/{id}/bewerken")
    public String showBewerkenForm(@PathVariable Long id, Model model) {
        model.addAttribute("baristaInputDTO", baristaService.findInputById(id));
        addFormAttributes(model);
        return "barista-form";
    }


    @PostMapping("/{id}/bewerken")
    public String processBewerkenForm(@PathVariable Long id,
                                      @Valid BaristaInputDTO baristaInputDTO,
                                      BindingResult result, Model model) {
        // als er fouten zijn keer je terug naar hetzelfde scherm
        if (result.hasErrors()) {
            addFormAttributes(model);
            return "barista-form";
        }

        // geen fouten, mag doorgaan
        baristaService.updateBarista(id, baristaInputDTO);
        return "redirect:/barista/" + id;
    }

    // toont de shiftpagina van een barista (beschikbare shifts + eigen shifts)
    // findDetailById regelt de autorisatie (enkel admin of de barista zelf), dus we hergebruiken die
    @GetMapping("/{id}/shifts")
    public String showShifts(@PathVariable Long id, Model model, Authentication authentication) {
        BaristaDetailDTO barista = baristaService.findDetailById(id, authentication.getName(), isAdmin(authentication));

        model.addAttribute("barista", barista);
        model.addAttribute("beschikbareShifts", shiftService.findBeschikbaarVoorBarista(id));
        model.addAttribute("eigenShifts", shiftService.findEigenShiften(id));
        return "shift-overview";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // navbar "Mijn shifts" knop - herleidt naar de shiftpagina van de ingelogde gebruiker zelf
    @GetMapping("/mijn-shifts")
    public String redirectNaarEigenShifts(Authentication authentication) {
        Long id = baristaService.findIdByEmail(authentication.getName());
        return "redirect:/barista/" + id + "/shifts";
    }

    @GetMapping("/mijn-profiel")
    public String redirectNaarEigenProfiel(Authentication authentication) {
        Long id = baristaService.findIdByEmail(authentication.getName());
        return "redirect:/barista/" + id;
    }

    // gemeenschappelijk model-attribuut voor het formulier (GET en POST bij fouten)
    private void addFormAttributes(Model model) {
        model.addAttribute("vestigingen", vestigingService.findAll());
    }
}
