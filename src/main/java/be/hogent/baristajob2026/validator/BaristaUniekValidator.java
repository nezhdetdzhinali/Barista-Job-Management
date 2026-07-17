package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.service.BaristaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


// voor email en studentenkaartnummer moet er een unieke barista zijn (dus geen andere barista met hetzelfde email of studentenkaartnummer)
// check over twee velden, dus geen annotation, maar een validator klasse
@Component // geen naam nodig want wij gebruiken advice
@RequiredArgsConstructor
public class BaristaUniekValidator implements Validator {

    private final BaristaService baristaService;

    // over welke object gaat her hier, welke type wordt gedefineerd in supports
    @Override
    public boolean supports(Class<?> klass) {
        return BaristaInputDTO.class.isAssignableFrom(klass);
    }

    // je geeft een object van jouw record = target
    // en de errors van de binding result = errors
    @Override
    public void validate(Object target, Errors errors) {
        // we gaan niet met object werken dus type casting
        BaristaInputDTO dto = (BaristaInputDTO) target;


        if (dto.email() != null && baristaService.bestaatAndereBaristaMetEmail(dto.email(), dto.id())) {
            errors.rejectValue("email", "duplicate.barista.email", "Dit e-mailadres is al in gebruik.");
        }

        if (dto.studentenkaartNummer() != null
                && baristaService.bestaatAndereBaristaMetStudentenkaart(dto.studentenkaartNummer(), dto.id())) {
            errors.rejectValue("studentenkaartNummer", "duplicate.barista.studentenkaart",
                    "Dit studentenkaartnummer is al in gebruik.");
        }
    }
}