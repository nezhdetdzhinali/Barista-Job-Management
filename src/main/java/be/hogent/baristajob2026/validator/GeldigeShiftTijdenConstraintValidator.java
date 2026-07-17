package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.ShiftInputDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;

public class GeldigeShiftTijdenConstraintValidator implements ConstraintValidator<GeldigeShiftTijden, ShiftInputDTO> {
    private static final int MIN_UUR = 2;
    private static final int MAX_UUR = 8;


    @Override
    public void initialize(GeldigeShiftTijden constraintAnnotation) {}

    @Override
    public boolean isValid(ShiftInputDTO dto, ConstraintValidatorContext context) {
        // niet-ingevulde velden zijn de taak van @NotNull, niet van deze validator
        if (dto.startUur() == null || dto.eindUur() == null) {
            return true;
        }

        // startuur < einduur
        if (!dto.startUur().isBefore(dto.eindUur())) {
            context.disableDefaultConstraintViolation(); // default message van spring uitzetten
            // als er een fout is, wil ik dat zien op het eindUur field
            context.buildConstraintViolationWithTemplate("{shift.tijden.volgorde}")
                    .addPropertyNode("eindUur")
                    .addConstraintViolation();
            return false;
        }

        // duur van een shift: minimaal 2 uur, maximaal 8 uur
        long duurInMinuten = Duration.between(dto.startUur(), dto.eindUur()).toMinutes();
        if (duurInMinuten < MIN_UUR * 60 || duurInMinuten > MAX_UUR * 60) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{shift.tijden.duur}")
                    .addPropertyNode("eindUur")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
