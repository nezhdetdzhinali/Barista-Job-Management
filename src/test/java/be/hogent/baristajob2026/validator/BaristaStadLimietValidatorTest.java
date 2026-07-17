package be.hogent.baristajob2026.validator;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.init.BuilderBaristaDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.VestigingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static be.hogent.baristajob2026.init.InitBarista.OK_VESTIGING_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class BaristaStadLimietValidatorTest {

    private Validator baristaStadLimietValidator;
    private BaristaService mockBaristaService;
    private VestigingService mockVestigingService;

    @BeforeEach
    void beforeEach() {
        mockBaristaService = mock(BaristaService.class);
        mockVestigingService = mock(VestigingService.class);
        baristaStadLimietValidator = new BaristaStadLimietValidator(mockBaristaService, mockVestigingService);
    }



    @Test
    void testValid_onderDeLimiet() {
        BaristaInputDTO barista = BuilderBaristaDTO.builder().actief(true).build().toDTO();
        when(mockVestigingService.findStadById(OK_VESTIGING_ID)).thenReturn("Gent");
        // 49 + de nieuwe barista = 50, precies op de limiet -> nog geldig
        when(mockBaristaService.telActieveBaristasInStad("Gent", barista.id())).thenReturn(49L);
        Errors errors = new BeanPropertyBindingResult(barista, "baristaInputDTO");

        baristaStadLimietValidator.validate(barista, errors);

        assertThat(errors.getAllErrors()).isEmpty();
    }

    @Test
    void testInvalid_boverDeLimiet() {
        BaristaInputDTO barista = BuilderBaristaDTO.builder().actief(true).build().toDTO();
        when(mockVestigingService.findStadById(OK_VESTIGING_ID)).thenReturn("Gent");
        // 50 + de nieuwe barista = 51, boven de limiet van 50
        when(mockBaristaService.telActieveBaristasInStad("Gent", barista.id())).thenReturn(50L);
        Errors errors = new BeanPropertyBindingResult(barista, "baristaInputDTO");

        baristaStadLimietValidator.validate(barista, errors);

        assertThat(errors.getAllErrors()).isNotEmpty();
        assertThat(errors.getErrorCount()).isEqualTo(1);
        assertThat(errors.getFieldError("vestigingId")).isNotNull();
    }

    @Test
    void testValid_vestigingNietGevonden() {
        // arrange: als de vestiging niet (meer) bestaat, wordt de check overgeslagen (geen stad om te tellen)
        BaristaInputDTO barista = BuilderBaristaDTO.builder().actief(true).build().toDTO();
        when(mockVestigingService.findStadById(OK_VESTIGING_ID)).thenReturn(null);
        Errors errors = new BeanPropertyBindingResult(barista, "baristaInputDTO");

        baristaStadLimietValidator.validate(barista, errors);

        assertThat(errors.getAllErrors()).isEmpty();
        verify(mockBaristaService, never()).telActieveBaristasInStad(any(), any());
    }

    @Test
    void testValid_baristaNietActief() {
        // arrange: als de barista niet actief wordt, wordt de limiet-check overgeslagen
        BaristaInputDTO barista = BuilderBaristaDTO.builder().actief(false).build().toDTO();
        Errors errors = new BeanPropertyBindingResult(barista, "baristaInputDTO");

        baristaStadLimietValidator.validate(barista, errors);

        assertThat(errors.getAllErrors()).isEmpty();
        verify(mockVestigingService, never()).findStadById(any());
    }
}
