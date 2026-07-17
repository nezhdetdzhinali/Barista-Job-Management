package be.hogent.baristajob2026.advice;

import be.hogent.baristajob2026.controller.BaristaController;
import be.hogent.baristajob2026.validator.BaristaStadLimietValidator;
import be.hogent.baristajob2026.validator.BaristaUniekValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

// voor BaristaUniekValidator
// in welke controller klasse wil je de validator oproepen?
@ControllerAdvice(assignableTypes = BaristaController.class)
// om te injecteren
@RequiredArgsConstructor
public class BaristaValidatorAdvice {
    // injectie
    private final BaristaUniekValidator baristaUniekValidator; // check op unieke email en studentenkaartnummer
    private final BaristaStadLimietValidator baristaStadLimietValidator; // max 50 actieve barista's in totaal per stad

    // effectief koppelen, de object dat ik heb geinjecteerd moet gebruikt worden in de controller
    @InitBinder("baristaInputDTO")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(baristaUniekValidator, baristaStadLimietValidator);
    }


}
