package be.hogent.baristajob2026.advice;

import be.hogent.baristajob2026.controller.VestigingController;
import be.hogent.baristajob2026.validator.VestigingUniekValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

// voor vestiginregels extra validatie; geen twee vestigingen met dezelfde naam + stad combo
@ControllerAdvice(assignableTypes = VestigingController.class)
@RequiredArgsConstructor
public class VestigingValidatorAdvice {
    private final VestigingUniekValidator vestigingUniekValidator;

    @InitBinder("vestigingInputDTO")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(vestigingUniekValidator);
    }
}
