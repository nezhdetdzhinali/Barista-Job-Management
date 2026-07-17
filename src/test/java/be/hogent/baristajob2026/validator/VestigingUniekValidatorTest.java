package be.hogent.baristajob2026.validator;


import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import be.hogent.baristajob2026.init.BuilderVestigingDTO;
import be.hogent.baristajob2026.service.VestigingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VestigingUniekValidatorTest {

    private Validator vestigingUniekValidator;
    private VestigingService mockVestigingService;

    @BeforeEach
    void beforeEach() {
        mockVestigingService = mock(VestigingService.class);
        vestigingUniekValidator = new VestigingUniekValidator(mockVestigingService);
    }

    @Test
    void testValid_uniekeNaamStadCombinatie() {
        VestigingInputDTO validVestiging = BuilderVestigingDTO.builder().build().toDTO();
        when(mockVestigingService.bestaatAndereVestigingMetNaamEnStad(
                validVestiging.naam(), validVestiging.stad(), validVestiging.id())).thenReturn(false);
        Errors errors = new BeanPropertyBindingResult(validVestiging, "vestigingInputDTO");

        vestigingUniekValidator.validate(validVestiging, errors);

        assertThat(errors.getAllErrors()).isEmpty();
    }

    @Test
    void testInvalid_dubbeleNaamStadCombinatie() {
        VestigingInputDTO invalidVestiging = BuilderVestigingDTO.builder().build().toDTO();
        when(mockVestigingService.bestaatAndereVestigingMetNaamEnStad(
                invalidVestiging.naam(), invalidVestiging.stad(), invalidVestiging.id())).thenReturn(true);
        Errors errors = new BeanPropertyBindingResult(invalidVestiging, "vestigingInputDTO");

        vestigingUniekValidator.validate(invalidVestiging, errors);

        assertThat(errors.getAllErrors()).isNotEmpty();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("naam")).isNotNull();
    }
}
