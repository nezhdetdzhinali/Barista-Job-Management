package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.validator.GeldigeLeeftijd;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// barista toevoegen/wijzigen form input
public record BaristaInputDTO(
        Long id,

        //  Voornaam en achternaam moeten met een hoofdletter beginnen en minstens 2 letters bevatten
        @Pattern(regexp = "^[A-Z][a-zA-Z]+$", message = "{barista.naam.pattern}")
        String voornaam,

        @Pattern(regexp = "^[A-Z][a-zA-Z]+$", message = "{barista.naam.pattern}")
        String achternaam,

        // exact 1 '@', niet als eerste/laatste teken toegelaten
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+$", message = "{barista.email.pattern}")
        String email,

        // leeftijd tussen 16 en 30 jaar
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @GeldigeLeeftijd(min = 16, max = 30, message = "{barista.leeftijd.range}")
        LocalDate geboortedatum,

        @Pattern(regexp = "^\\d{8}$", message = "{barista.studentenkaart.pattern}")
        String studentenkaartNummer,

        Boolean actief,

        @NotNull(message = "{barista.vestiging.required}")
        Long vestigingId

        ) {
}
