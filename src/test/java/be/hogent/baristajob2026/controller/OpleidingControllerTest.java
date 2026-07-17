package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.OpleidingInputDTO;
import be.hogent.baristajob2026.exception.OpleidingNotFoundException;
import be.hogent.baristajob2026.init.BuilderOpleidingDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
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

import static be.hogent.baristajob2026.init.InitOpleiding.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OpleidingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({BaristaUniekValidator.class, BaristaStadLimietValidator.class, VestigingUniekValidator.class})
public class OpleidingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpleidingService mockService;

    @MockitoBean
    private VestigingService vestigingService;

    @MockitoBean
    private BaristaService baristaService;

    // get request van opleiding crud form

    @Test
    void testGetNieuwForm() throws Exception {
        mockMvc.perform(get("/opleiding/nieuw"))
                .andExpect(status().isOk())
                .andExpect(view().name("opleiding-form"))
                .andExpect(model().attributeExists("opleidingInputDTO"))
                .andExpect(model().attributeExists("vestigingen"));
    }

    // post request van opleiding crud form, geldig

    @Test
    void testPostNieuwForm_valid() throws Exception {
        doNothing().when(mockService).createOpleiding(any(OpleidingInputDTO.class));

        OpleidingInputDTO validOpleiding = BuilderOpleidingDTO.builder().build().toDTO();

        mockMvc.perform(post("/opleiding/nieuw").flashAttr("opleidingInputDTO", validOpleiding))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/opleiding"));

        verify(mockService).createOpleiding(any());
    }

    private static Stream<Arguments> invalidOpleidingData() {
        return Stream.of(
                Arguments.of(null, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, new String[]{"titel"}),
                Arguments.of("", OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, new String[]{"titel"}),
                Arguments.of(OK_TITEL, null, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, new String[]{"beschrijving"}),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, null, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, new String[]{"datum"}),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, 0, OK_MAX_DEELNEMERS, OK_VESTIGING_ID, new String[]{"duurInUur"}),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, 0, OK_VESTIGING_ID, new String[]{"maxDeelnemers"}),
                Arguments.of(OK_TITEL, OK_BESCHRIJVING, OK_DATUM, OK_DUUR_IN_UUR, OK_MAX_DEELNEMERS, null, new String[]{"vestigingId"}),
                Arguments.of(null, "", null, 0, 0, null, new String[]{"titel", "beschrijving", "datum", "duurInUur", "maxDeelnemers", "vestigingId"})
        );
    }

    // post request van opleiding crud form, ongeldig
    @ParameterizedTest
    @MethodSource("invalidOpleidingData")
    void testPostNieuwForm_invalid(String titel, String beschrijving, LocalDate datum, Integer duurInUur,
                                   Integer maxDeelnemers, Long vestigingId, String[] expectedErrors) throws Exception {
        OpleidingInputDTO invalidOpleiding = BuilderOpleidingDTO.builder()
                .titel(titel).beschrijving(beschrijving).datum(datum).duurInUur(duurInUur)
                .maxDeelnemers(maxDeelnemers).vestigingId(vestigingId).build().toDTO();

        mockMvc.perform(post("/opleiding/nieuw").flashAttr("opleidingInputDTO", invalidOpleiding))
                .andExpect(status().isOk())
                .andExpect(view().name("opleiding-form"))
                .andExpect(model().attributeHasFieldErrors("opleidingInputDTO", expectedErrors));

        verify(mockService, never()).createOpleiding(any());
    }

    // get request van bewerken form geldig
    @Test
    void testGetBewerkenForm() throws Exception {
        OpleidingInputDTO expected = BuilderOpleidingDTO.builder().build().toDTO();
        when(mockService.findInputById(1L)).thenReturn(expected);

        mockMvc.perform(get("/opleiding/1/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("opleiding-form"))
                .andExpect(model().attributeExists("opleidingInputDTO"))
                .andExpect(model().attribute("opleidingInputDTO", expected));

        verify(mockService).findInputById(1L);
    }

    // get request van bewerken form, opleiding niet gevonden
    @Test
    void testGetBewerkenForm_notFound() throws Exception {
        when(mockService.findInputById(1L)).thenThrow(new OpleidingNotFoundException(1L));

        mockMvc.perform(get("/opleiding/1/bewerken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));
    }

    // get request van bewerken form, ongeldig id formaat
    @Test
    void testInvalidIdFormat_bewerken() throws Exception {
        mockMvc.perform(get("/opleiding/abc/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(mockService, never()).findInputById(any());
    }

    // post request van bewerken form, geldig

    @Test
    void testPostBewerkenForm_valid() throws Exception {
        doNothing().when(mockService).updateOpleiding(eq(1L), any(OpleidingInputDTO.class));

        OpleidingInputDTO validOpleiding = BuilderOpleidingDTO.builder().build().toDTO();

        mockMvc.perform(post("/opleiding/1/bewerken").flashAttr("opleidingInputDTO", validOpleiding))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/opleiding"));

        verify(mockService).updateOpleiding(eq(1L), any());
    }

    // post request van bewerken form, ongeldig
    @ParameterizedTest
    @MethodSource("invalidOpleidingData")
    void testPostBewerkenForm_invalid(String titel, String beschrijving, LocalDate datum, Integer duurInUur,
                                      Integer maxDeelnemers, Long vestigingId, String[] expectedErrors) throws Exception {
        OpleidingInputDTO invalidOpleiding = BuilderOpleidingDTO.builder()
                .titel(titel).beschrijving(beschrijving).datum(datum).duurInUur(duurInUur)
                .maxDeelnemers(maxDeelnemers).vestigingId(vestigingId).build().toDTO();

        mockMvc.perform(post("/opleiding/1/bewerken").flashAttr("opleidingInputDTO", invalidOpleiding))
                .andExpect(status().isOk())
                .andExpect(view().name("opleiding-form"))
                .andExpect(model().attributeHasFieldErrors("opleidingInputDTO", expectedErrors));

        verify(mockService, never()).updateOpleiding(any(), any());
    }
}
