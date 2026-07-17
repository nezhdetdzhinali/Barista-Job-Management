package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.init.BuilderBaristaDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.ShiftService;
import be.hogent.baristajob2026.service.VestigingService;
import be.hogent.baristajob2026.validator.BaristaStadLimietValidator;
import be.hogent.baristajob2026.validator.BaristaUniekValidator;
import be.hogent.baristajob2026.validator.VestigingUniekValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.stream.Stream;
import static be.hogent.baristajob2026.init.InitBarista.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BaristaController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({BaristaUniekValidator.class, BaristaStadLimietValidator.class, VestigingUniekValidator.class})
public class BaristaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BaristaService mockService;

    @MockitoBean
    private VestigingService vestigingService;

    @MockitoBean
    private ShiftService shiftService;

    @MockitoBean
    private OpleidingService opleidingService;

    // barista form base get request (form tonen)
    @Test
    void testGetNieuwForm() throws Exception {
        mockMvc.perform(get("/barista/nieuw"))
                .andExpect(status().isOk())
                .andExpect(view().name("barista-form"))
                .andExpect(model().attributeExists("baristaInputDTO"))
                .andExpect(model().attributeExists("vestigingen"));
    }

    // barista form base post request - valid (barista aanmaken)

    @Test
    void testPostNieuwForm_valid() throws Exception {
        doNothing().when(mockService).createBarista(any(BaristaInputDTO.class));

        BaristaInputDTO validBarista = BuilderBaristaDTO.builder().build().toDTO();

        mockMvc.perform(post("/barista/nieuw").flashAttr("baristaInputDTO", validBarista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));

        verify(mockService).createBarista(any());
    }

    // ongeldige barista data voor parameterized test gebaseerd op de annotations in de barista form dto (BaristaInputDTO)

    private static Stream<Arguments> invalidBaristaData() {
        return Stream.of(
                // voornaam moet met hoofdletter beginnen + min. 2 letters
                Arguments.of("jan", OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"voornaam"}),
                Arguments.of("J", OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"voornaam"}),
                // achternaam analoog
                Arguments.of(OK_VOORNAAM, "peeters", OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"achternaam"}),
                // email zonder '@'
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, "ongeldig-email", OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"email"}),
                // leeftijd buiten 16-30 jaar
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(10), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"geboortedatum"}),
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, LocalDate.now().minusYears(40), OK_STUDENTENKAARTNUMMER, OK_VESTIGING_ID, new String[]{"geboortedatum"}),
                // studentenkaartnummer moet exact 8 cijfers zijn
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, "123", OK_VESTIGING_ID, new String[]{"studentenkaartNummer"}),
                // vestigingId verplicht
                Arguments.of(OK_VOORNAAM, OK_ACHTERNAAM, OK_EMAIL, OK_GEBOORTEDATUM, OK_STUDENTENKAARTNUMMER, null, new String[]{"vestigingId"}),
                // combinatie van meerdere fouten
                Arguments.of("jan", "peeters", "ongeldig-email", LocalDate.now().minusYears(10), "123", null,
                        new String[]{"voornaam", "achternaam", "email", "geboortedatum", "studentenkaartNummer", "vestigingId"})
        );
    }

    // barista form base post request - invalid (barista aanmaken)
    @ParameterizedTest
    @MethodSource("invalidBaristaData")
    void testPostNieuwForm_invalid(String voornaam, String achternaam, String email, LocalDate geboortedatum,
                                   String studentenkaartNummer, Long vestigingId, String[] expectedErrors) throws Exception {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder()
                .voornaam(voornaam).achternaam(achternaam).email(email).geboortedatum(geboortedatum)
                .studentenkaartNummer(studentenkaartNummer).vestigingId(vestigingId).build().toDTO();

        mockMvc.perform(post("/barista/nieuw").flashAttr("baristaInputDTO", invalidBarista))
                .andExpect(status().isOk())
                .andExpect(view().name("barista-form"))
                .andExpect(model().attributeHasFieldErrors("baristaInputDTO", expectedErrors));

        verify(mockService, never()).createBarista(any());
    }

    // bestaande barista bewerken form get request (form tonen)
    @Test
    void testGetBewerkenForm() throws Exception {
        BaristaInputDTO expected = BuilderBaristaDTO.builder().build().toDTO();
        when(mockService.findInputById(1L)).thenReturn(expected);

        mockMvc.perform(get("/barista/1/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("barista-form"))
                .andExpect(model().attributeExists("baristaInputDTO"))
                .andExpect(model().attribute("baristaInputDTO", expected));

        verify(mockService).findInputById(1L);
    }

    // onbestaande barista bewerken form get request (form tonen) - barista niet gevonden
    @Test
    void testGetBewerkenForm_notFound() throws Exception {
        when(mockService.findInputById(1L)).thenThrow(new BaristaNotFoundException(1L));

        mockMvc.perform(get("/barista/1/bewerken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));
    }

    // ongeldige barista id (niet-numeriek) bewerken form get request (form tonen) - onbestaande pagina
    @Test
    void testInvalidIdFormat_bewerken() throws Exception {
        mockMvc.perform(get("/barista/abc/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(mockService, never()).findInputById(any());
    }

    // barista bewerken post request - geldig
    @Test
    void testPostBewerkenForm_valid() throws Exception {
        doNothing().when(mockService).updateBarista(eq(1L), any(BaristaInputDTO.class));

        BaristaInputDTO validBarista = BuilderBaristaDTO.builder().build().toDTO();

        mockMvc.perform(post("/barista/1/bewerken").flashAttr("baristaInputDTO", validBarista))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/barista/1"));

        verify(mockService).updateBarista(eq(1L), any());
    }

    // barista bewerken post request - ongeldig
    @ParameterizedTest
    @MethodSource("invalidBaristaData")
    void testPostBewerkenForm_invalid(String voornaam, String achternaam, String email, LocalDate geboortedatum,
                                      String studentenkaartNummer, Long vestigingId, String[] expectedErrors) throws Exception {
        BaristaInputDTO invalidBarista = BuilderBaristaDTO.builder()
                .voornaam(voornaam).achternaam(achternaam).email(email).geboortedatum(geboortedatum)
                .studentenkaartNummer(studentenkaartNummer).vestigingId(vestigingId).build().toDTO();

        mockMvc.perform(post("/barista/1/bewerken").flashAttr("baristaInputDTO", invalidBarista))
                .andExpect(status().isOk())
                .andExpect(view().name("barista-form"))
                .andExpect(model().attributeHasFieldErrors("baristaInputDTO", expectedErrors));

        verify(mockService, never()).updateBarista(any(), any());
    }

}
