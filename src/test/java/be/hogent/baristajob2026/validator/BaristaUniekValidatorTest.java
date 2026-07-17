package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.init.BuilderBaristaDTO;
import be.hogent.baristajob2026.service.BaristaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaristaUniekValidatorTest {
    private Validator baristaUniekValidator;
    private BaristaService mockBaristaService;

    @BeforeEach
    void beforeEach() {
        mockBaristaService = mock(BaristaService.class);
        baristaUniekValidator = new BaristaUniekValidator(mockBaristaService);
    }

    @Test
    void testValidBarista_uniekeGegevens() {
        // arrange: geen andere barista gebruikt dit email of studentenkaartnummer
        BaristaInputDTO validBarista = BuilderBaristaDTO.builder().build().toDTO();
        when(mockBaristaService.bestaatAndereBaristaMetEmail(validBarista.email(), validBarista.id())).thenReturn(false);
        when(mockBaristaService.bestaatAndereBaristaMetStudentenkaart(validBarista.studentenkaartNummer(), validBarista.id())).thenReturn(false);
        Errors errors = new BeanPropertyBindingResult(validBarista, "baristaInputDTO");

        // act
        baristaUniekValidator.validate(validBarista, errors);

        // assert: we verwachten geen fouten
        assertThat(errors.getAllErrors()).isEmpty();
    }

    @Test
    void testInvalidBarista_dubbelEmail() {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder().build().toDTO();
        when(mockBaristaService.bestaatAndereBaristaMetEmail(invalidBarista.email(), invalidBarista.id())).thenReturn(true);
        when(mockBaristaService.bestaatAndereBaristaMetStudentenkaart(invalidBarista.studentenkaartNummer(), invalidBarista.id())).thenReturn(false);
        Errors errors = new BeanPropertyBindingResult(invalidBarista, "baristaInputDTO");

        baristaUniekValidator.validate(invalidBarista, errors);

        // we verwachten 1 fout, op het veld email
        assertThat(errors.getAllErrors()).isNotEmpty();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("email")).isNotNull();
    }

    @Test
    void testInvalidBarista_dubbelStudentenkaartnummer() {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder().build().toDTO();
        when(mockBaristaService.bestaatAndereBaristaMetEmail(invalidBarista.email(), invalidBarista.id())).thenReturn(false);
        when(mockBaristaService.bestaatAndereBaristaMetStudentenkaart(invalidBarista.studentenkaartNummer(), invalidBarista.id())).thenReturn(true);
        Errors errors = new BeanPropertyBindingResult(invalidBarista, "baristaInputDTO");

        baristaUniekValidator.validate(invalidBarista, errors);

        assertThat(errors.getAllErrors()).isNotEmpty();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("studentenkaartNummer")).isNotNull();
    }

    // beide velden zijn niet uniek
    @Test
    void testInvalidBarista_dubbelEmailEnStudentenkaartnummer() {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder().build().toDTO();
        when(mockBaristaService.bestaatAndereBaristaMetEmail(invalidBarista.email(), invalidBarista.id())).thenReturn(true);
        when(mockBaristaService.bestaatAndereBaristaMetStudentenkaart(invalidBarista.studentenkaartNummer(), invalidBarista.id())).thenReturn(true);
        Errors errors = new BeanPropertyBindingResult(invalidBarista, "baristaInputDTO");

        baristaUniekValidator.validate(invalidBarista, errors);


        assertThat(errors.getErrorCount()).isEqualTo(2);
        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("studentenkaartNummer")).isNotNull();
    }
}
