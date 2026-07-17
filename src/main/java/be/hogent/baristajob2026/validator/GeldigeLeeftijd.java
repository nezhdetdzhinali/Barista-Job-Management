package be.hogent.baristajob2026.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// voor java documentatie
@Documented
// wat is de validatie? we verwijzen naar een klasse waar de voorwaarden zijn
@Constraint(validatedBy = GeldigeLeeftijdConstraintValidator.class)
// boven wat kunnen wij de annotation plaatsen? we zeggen velden hier
@Target({FIELD})
// tot wanneer moet de annotation bestaan? hij bestaat hier in runtime anders zouden wij hem niet kunnen gebruiken
@Retention(RUNTIME)
public @interface GeldigeLeeftijd {

    int min() default 16;
    int max() default 30;

    String message() default "leeftijd moet tussen {min} en {max} jaar liggen";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}