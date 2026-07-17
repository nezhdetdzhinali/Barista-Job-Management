package be.hogent.baristajob2026.dto.request;

import be.hogent.baristajob2026.init.BuilderVestigingDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static be.hogent.baristajob2026.init.InitVestiging.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VestigingInputDTOTest {
    private Validator validator;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static Stream<Arguments> validVestigingData() {
        return Stream.of(
                Arguments.of(OK_NAAM, OK_STAD, OK_AANTAL_ZITPLAATSEN),
                Arguments.of(OK_NAAM, OK_STAD, 1) // ondergrens zitplaatsen
        );
    }

    @ParameterizedTest
    @MethodSource("validVestigingData")
    void testValidVestiging(String naam, String stad, Integer aantalZitplaatsen) {
        VestigingInputDTO validVestiging = BuilderVestigingDTO.builder()
                .naam(naam).stad(stad).aantalZitplaatsen(aantalZitplaatsen).build().toDTO();

        Set<ConstraintViolation<VestigingInputDTO>> violations = validator.validate(validVestiging);

        assertThat(violations).isEmpty();
    }

    private static Stream<Arguments> invalidVestigingData() {
        return Stream.of(
                Arguments.of(null, OK_STAD, OK_AANTAL_ZITPLAATSEN, "naam"),
                Arguments.of("", OK_STAD, OK_AANTAL_ZITPLAATSEN, "naam"),
                Arguments.of(OK_NAAM, null, OK_AANTAL_ZITPLAATSEN, "stad"),
                Arguments.of(OK_NAAM, OK_STAD, null, "aantalZitplaatsen"),
                Arguments.of(OK_NAAM, OK_STAD, 0, "aantalZitplaatsen")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidVestigingData")
    void testInvalidVestiging(String naam, String stad, Integer aantalZitplaatsen, String expected) {
        VestigingInputDTO invalidVestiging = BuilderVestigingDTO.builder()
                .naam(naam).stad(stad).aantalZitplaatsen(aantalZitplaatsen).build().toDTO();

        Set<ConstraintViolation<VestigingInputDTO>> violations = validator.validate(invalidVestiging);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(expected));
    }
}
