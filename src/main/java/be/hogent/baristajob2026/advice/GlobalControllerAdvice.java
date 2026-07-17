package be.hogent.baristajob2026.advice;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

// een verseiste is dat elke pagina de username en de rol van de ingelogde gebruiker kan tonen
// deze advice klasse geeft de attributen mee aan elke controller zodat ze in de view gebruikt kunnen worden
// ipv in elke controller telkens deze attributen te moeten passen via addAttribute per view
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("username")
    public String populateUsername(Authentication authentication) {
        return authentication == null ? "": authentication.getName();
    }

    @ModelAttribute("roles")
    public List<String> populateRoles(Authentication authentication) {
        return authentication == null ? List.of(): authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();
    }

}