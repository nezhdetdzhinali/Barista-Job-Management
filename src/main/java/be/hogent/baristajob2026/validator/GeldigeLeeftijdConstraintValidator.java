package be.hogent.baristajob2026.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class GeldigeLeeftijdConstraintValidator implements ConstraintValidator<GeldigeLeeftijd, LocalDate> {

    private int min;
    private int max;

    @Override
    public void initialize(GeldigeLeeftijd constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate geboortedatum, ConstraintValidatorContext context) {
        // niet-ingevulde velden zijn de taak van @NotNull, niet van deze validator
        if (geboortedatum == null) {
            return true;
        }

        int leeftijd = Period.between(geboortedatum, LocalDate.now()).getYears();
        return leeftijd >= min && leeftijd <= max;
    }
}