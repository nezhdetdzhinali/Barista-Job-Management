package be.hogent.baristajob2026.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
// wat is de validatie? we verwijzen naar een klasse waar de voorwaarden zijn
@Constraint(validatedBy = GeldigeShiftTijdenConstraintValidator.class)
// klass niveau want we werken met 2 parameters, startuur en einduur
@Target({TYPE})
// tot wanneer moet de annotation bestaan? hij bestaat hier in runtime anders zouden wij hem niet kunnen gebruiken
@Retention(RUNTIME)
public @interface GeldigeShiftTijden {
    String message() default "Ongeldige shift tijden";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
