package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.init.BuilderShiftDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Stream;

import static be.hogent.baristajob2026.init.InitShift.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ShiftInputDTOTest {
    private Validator validator;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static Stream<Arguments> validShiftData() {
        return Stream.of(
                Arguments.of(OK_DATUM, OK_START_UUR, OK_EIND_UUR, OK_MAX_BARISTAS, OK_VESTIGING_ID),
                Arguments.of(OK_DATUM, LocalTime.of(9, 0), LocalTime.of(11, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID), // 2 uur, ondergrens
                Arguments.of(OK_DATUM, LocalTime.of(9, 0), LocalTime.of(17, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID)  // 8 uur, bovengrens
        );
    }

    @ParameterizedTest
    @MethodSource("validShiftData")
    void testValidShift(LocalDate datum, LocalTime startUur, LocalTime eindUur, Integer maxBaristas, Long vestigingId) {
        ShiftInputDTO validShift = BuilderShiftDTO.builder()
                .datum(datum).startUur(startUur).eindUur(eindUur).maxBaristas(maxBaristas).vestigingId(vestigingId)
                .build().toDTO();

        Set<ConstraintViolation<ShiftInputDTO>> violations = validator.validate(validShift);

        assertThat(violations).isEmpty();
    }

    private static Stream<Arguments> invalidShiftData() {
        return Stream.of(
                Arguments.of(null, OK_START_UUR, OK_EIND_UUR, OK_MAX_BARISTAS, OK_VESTIGING_ID, "datum"),
                Arguments.of(OK_DATUM, null, OK_EIND_UUR, OK_MAX_BARISTAS, OK_VESTIGING_ID, "startUur"),
                Arguments.of(OK_DATUM, OK_START_UUR, null, OK_MAX_BARISTAS, OK_VESTIGING_ID, "eindUur"),
                Arguments.of(OK_DATUM, OK_START_UUR, OK_EIND_UUR, 0, OK_VESTIGING_ID, "maxBaristas"),
                Arguments.of(OK_DATUM, OK_START_UUR, OK_EIND_UUR, OK_MAX_BARISTAS, null, "vestigingId"),
                // eindUur voor startUur (@GeldigeShiftTijden, de custom annotatie)
                Arguments.of(OK_DATUM, LocalTime.of(14, 0), LocalTime.of(10, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID, "eindUur"),
                // shift te kort (< 2 uur)
                Arguments.of(OK_DATUM, LocalTime.of(9, 0), LocalTime.of(9, 30), OK_MAX_BARISTAS, OK_VESTIGING_ID, "eindUur"),
                // shift te lang (> 8 uur)
                Arguments.of(OK_DATUM, LocalTime.of(8, 0), LocalTime.of(17, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID, "eindUur")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidShiftData")
    void testInvalidShift(LocalDate datum, LocalTime startUur, LocalTime eindUur, Integer maxBaristas,
                          Long vestigingId, String expected) {
        ShiftInputDTO invalidShift = BuilderShiftDTO.builder()
                .datum(datum).startUur(startUur).eindUur(eindUur).maxBaristas(maxBaristas).vestigingId(vestigingId)
                .build().toDTO();

        Set<ConstraintViolation<ShiftInputDTO>> violations = validator.validate(invalidShift);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(expected));
    }
}
