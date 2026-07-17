package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.init.BuilderOpleidingDTO;
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

import static be.hogent.baristajob2026.init.InitOpleiding.*;
import static org.assertj.core.api.Assertions.assertThat;
public class OpleidingInputDTOTest {

    private Validator validator;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static Stream<Arguments> validOpleidingData() {
        return Stream.of(
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, 1, 1, OK_VESTIGING_ID) // ondergrens duur/deelnemers
        );
    }

    @ParameterizedTest
    @MethodSource("validOpleidingData")
    void testValidOpleiding(String titel, String beschrijving, LocalDate datum, Integer duurInUur,
                            Integer maxDeelnemers, Long vestigingId) {
        OpleidingInputDTO validOpleiding = BuilderOpleidingDTO.builder()
                .titel(titel).beschrijving(beschrijving).datum(datum).duurInUur(duurInUur)
                .maxDeelnemers(maxDeelnemers).vestigingId(vestigingId).build().toDTO();

        Set<ConstraintViolation<OpleidingInputDTO>> violations = validator.validate(validOpleiding);

        assertThat(violations).isEmpty();
    }

    private static Stream<Arguments> invalidOpleidingData() {
        return Stream.of(
                Arguments.of(null, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, "titel"),
                Arguments.of("", OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, "titel"),
                Arguments.of(OK_TITEL, null, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, "beschrijving"),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, null, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, "datum"),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, 0, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, "duurInUur"),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, 0, OK_VESTIGING_ID, "maxDeelnemers"),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, null, "vestigingId")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidOpleidingData")
    void testInvalidOpleiding(String titel, String beschrijving, LocalDate datum, Integer duurInUur,
                              Integer maxDeelnemers, Long vestigingId, String expected) {
        OpleidingInputDTO invalidOpleiding = BuilderOpleidingDTO.builder()
                .titel(titel).beschrijving(beschrijving).datum(datum).duurInUur(duurInUur)
                .maxDeelnemers(maxDeelnemers).vestigingId(vestigingId).build().toDTO();

        Set<ConstraintViolation<OpleidingInputDTO>> violations = validator.validate(invalidOpleiding);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(expected));
    }
}
