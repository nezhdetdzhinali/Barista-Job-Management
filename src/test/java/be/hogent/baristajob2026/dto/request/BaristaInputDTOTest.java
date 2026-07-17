package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.init.BuilderBaristaDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static be.hogent.baristajob2026.init.InitBarista.*;
import static org.assertj.core.api.Assertions.assertThat;

// dit bestaat om de custom annotation @GeldigeLeeftijd te testen, zoals vereist in de opgave
public class BaristaInputDTOTest {
    private Validator validator;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static Stream<Arguments> validBaristaData() {
        return Stream.of(
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID),
                Arguments.of("Jan", "Jansens", OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID), // net lang genoeg
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(16), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID), // ondergrens leeftijd
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(30), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID) // bovengrens leeftijd
        );
    }

    @ParameterizedTest
    @MethodSource("validBaristaData")
    void testValidBarista(String voornaam, String achternaam, String email, LocalDate geboortedatum,
                          String studentenkaartNummer, Long vestigingId) {
        BaristaInputDTO validBarista = BuilderBaristaDTO.builder()
                .voornaam(voornaam).achternaam(achternaam).email(email).geboortedatum(geboortedatum)
                .studentenkaartNummer(studentenkaartNummer).vestigingId(vestigingId).build().toDTO();

        Set<ConstraintViolation<BaristaInputDTO>> violations = validator.validate(validBarista);

        assertThat(violations).isEmpty();
    }

    private static Stream<Arguments> invalidBaristaData() {
        return Stream.of(
                // voornaam moet met hoofdletter beginnen + min. 2 letters
                Arguments.of("jan", OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "voornaam"),
                Arguments.of("J", OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "voornaam"),
                // achternaam analoog
                Arguments.of(OK_VOORNAAM, "peeters", OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "achternaam"),
                // email zonder '@'
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, "ongeldig-email", OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "email"),
                // leeftijd buiten 16-30 jaar (@GeldigeLeeftijd, de custom annotatie)
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(15), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "geboortedatum"),
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(31), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, "geboortedatum"),
                // studentenkaartnummer moet exact 8 cijfers zijn
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, "123", OK_VESTIGING_ID, "studentenkaartNummer"),
                // vestigingId verplicht
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, null, "vestigingId")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidBaristaData")
    void testInvalidBarista(String voornaam, String achternaam, String email, LocalDate geboortedatum,
                            String studentenkaartNummer, Long vestigingId, String expected) {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder()
                .voornaam(voornaam).achternaam(achternaam).email(email).geboortedatum(geboortedatum)
                .studentenkaartNummer(studentenkaartNummer).vestigingId(vestigingId).build().toDTO();

        Set<ConstraintViolation<BaristaInputDTO>> violations = validator.validate(invalidBarista);

        // we verwachten fouten
        assertThat(violations).isNotEmpty();
        // de fout verwachten bij "expected"
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(expected));
    }
}
