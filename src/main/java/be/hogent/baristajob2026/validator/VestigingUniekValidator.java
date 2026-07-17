package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import be.hogent.baristajob2026.service.VestigingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

// geen twee vestigingen met dezelfde combinatie naam + stad
// check over twee velden samen, dus geen annotation maar een validator klasse
@Component
@RequiredArgsConstructor
public class VestigingUniekValidator implements Validator {
    private final VestigingService vestigingService;

    @Override
    public boolean supports(Class<?> klass) {
        return VestigingInputDTO.class.isAssignableFrom(klass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // we gaan niet met object werken dus type casting
        VestigingInputDTO dto = (VestigingInputDTO) target;

        if (dto.naam() != null && dto.stad() != null
                && vestigingService.bestaatAndereVestigingMetNaamEnStad(dto.naam(), dto.stad(), dto.id())) {
            errors.rejectValue("naam", "duplicate.vestiging.naamstad",
                    "Er bestaat al een vestiging met deze naam in deze stad.");
        }
    }
}

