package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.VestigingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// additionele businessregel: max 50 actieve barista's in totaal per stad
// vereist een db-query (tellen), dus geen annotation maar een validator klasse
@Component
@RequiredArgsConstructor
public class BaristaStadLimietValidator implements Validator {

    private static final int MAX_ACTIEVE_BARISTAS_PER_STAD = 50;

    private final BaristaService baristaService;
    private final VestigingService vestigingService;


    @Override
    public boolean supports(Class<?> klass) {
        return BaristaInputDTO.class.isAssignableFrom(klass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BaristaInputDTO dto = (BaristaInputDTO) target;

        boolean wordtActief = dto.actief() != null && dto.actief();

        if (!wordtActief || dto.vestigingId() == null) {
            return;
        }

        String stad = vestigingService.findStadById(dto.vestigingId());
        if (stad == null) {
            return;
        }

        long actieveInStad = baristaService.telActieveBaristasInStad(stad, dto.id());

        if (actieveInStad + 1 > MAX_ACTIEVE_BARISTAS_PER_STAD) {
            errors.rejectValue("vestigingId", "barista.stad.limiet",
                    "Er mogen maximaal %d actieve barista's per stad zijn.".formatted(MAX_ACTIEVE_BARISTAS_PER_STAD));
        }
    }
}
