package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.ShiftInputDTO;
import be.hogent.baristajob2026.exception.ShiftNotFoundException;
import be.hogent.baristajob2026.init.BuilderShiftDTO;
import be.hogent.baristajob2026.service.BaristaService;
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
import java.time.LocalTime;
import java.util.stream.Stream;

import static be.hogent.baristajob2026.init.InitShift.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShiftController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({BaristaUniekValidator.class, BaristaStadLimietValidator.class, VestigingUniekValidator.class})
public class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftService mockService;

    @MockitoBean
    private VestigingService vestigingService;

    @MockitoBean
    private BaristaService baristaService;

    // get request shift crud
    @Test
    void testGetNieuwForm() throws Exception {
        mockMvc.perform(get("/shift/nieuw"))
                .andExpect(status().isOk())
                .andExpect(view().name("shift-form"))
                .andExpect(model().attributeExists("shiftInputDTO"))
                .andExpect(model().attributeExists("vestigingen"))
                .andExpect(model().attributeExists("shiftRollen"));
    }

    // post request shift crud geldig
    @Test
    void testPostNieuwForm_valid() throws Exception {
        doNothing().when(mockService).createShift(any(ShiftInputDTO.class));

        ShiftInputDTO validShift = BuilderShiftDTO.builder().build().toDTO();

        mockMvc.perform(post("/shift/nieuw").flashAttr("shiftInputDTO", validShift))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vestiging/" + OK_VESTIGING_ID));

        verify(mockService).createShift(any());
    }

    private static Stream<Arguments> invalidShiftData() {
        return Stream.of(
                Arguments.of(null, OK_START_UUR, OK_EIND_UUR, OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"datum"}),
                Arguments.of(OK_DATUM, null, OK_EIND_UUR, OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"startUur"}),
                Arguments.of(OK_DATUM, OK_START_UUR, null, OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"eindUur"}),
                Arguments.of(OK_DATUM, OK_START_UUR, OK_EIND_UUR, 0, OK_VESTIGING_ID, new String[]{"maxBaristas"}),
                Arguments.of(OK_DATUM, OK_START_UUR, OK_EIND_UUR, OK_MAX_BARISTAS, null, new String[]{"vestigingId"}),
                // eindUur voor startUur -> @GeldigeShiftTijden foutmelding staat op eindUur (cfr. addPropertyNode("eindUur") in de constraintvalidator)
                Arguments.of(OK_DATUM, LocalTime.of(14, 0), LocalTime.of(10, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"eindUur"}),
                // shift te kort (< 2 uur)
                Arguments.of(OK_DATUM, LocalTime.of(9, 0), LocalTime.of(9, 30), OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"eindUur"}),
                // shift te lang (> 8 uur)
                Arguments.of(OK_DATUM, LocalTime.of(8, 0), LocalTime.of(17, 0), OK_MAX_BARISTAS, OK_VESTIGING_ID, new String[]{"eindUur"})
        );
    }

    @ParameterizedTest
    @MethodSource("invalidShiftData")
    void testPostNieuwForm_invalid(LocalDate datum, LocalTime startUur, LocalTime eindUur, Integer maxBaristas,
                                   Long vestigingId, String[] expectedErrors) throws Exception {
        ShiftInputDTO invalidShift = BuilderShiftDTO.builder()
                .datum(datum).startUur(startUur).eindUur(eindUur).maxBaristas(maxBaristas).vestigingId(vestigingId)
                .build().toDTO();

        mockMvc.perform(post("/shift/nieuw").flashAttr("shiftInputDTO", invalidShift))
                .andExpect(status().isOk())
                .andExpect(view().name("shift-form"))
                .andExpect(model().attributeHasFieldErrors("shiftInputDTO", expectedErrors));

        verify(mockService, never()).createShift(any());
    }

    // get shift bewerken pagina
    @Test
    void testGetBewerkenForm() throws Exception {
        ShiftInputDTO expected = BuilderShiftDTO.builder().build().toDTO();
        when(mockService.findInputById(1L)).thenReturn(expected);

        mockMvc.perform(get("/shift/1/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("shift-form"))
                .andExpect(model().attributeExists("shiftInputDTO"))
                .andExpect(model().attribute("shiftInputDTO", expected));

        verify(mockService).findInputById(1L);
    }

    // get shift bewerken pagina - niet gevonden
    @Test
    void testGetBewerkenForm_notFound() throws Exception {
        when(mockService.findInputById(1L)).thenThrow(new ShiftNotFoundException(1L));

        mockMvc.perform(get("/shift/1/bewerken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));
    }

    // get shift bewerken pagina - ongeldig id formaat
    @Test
    void testInvalidIdFormat_bewerken() throws Exception {
        mockMvc.perform(get("/shift/abc/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(mockService, never()).findInputById(any());
    }

    // post request shift bewerken geldig
    @Test
    void testPostBewerkenForm_valid() throws Exception {
        doNothing().when(mockService).updateShift(eq(1L), any(ShiftInputDTO.class));

        ShiftInputDTO validShift = BuilderShiftDTO.builder().build().toDTO();

        mockMvc.perform(post("/shift/1/bewerken").flashAttr("shiftInputDTO", validShift))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vestiging/" + OK_VESTIGING_ID));

        verify(mockService).updateShift(eq(1L), any());
    }

    @ParameterizedTest
    @MethodSource("invalidShiftData")
    void testPostBewerkenForm_invalid(LocalDate datum, LocalTime startUur, LocalTime eindUur, Integer maxBaristas,
                                      Long vestigingId, String[] expectedErrors) throws Exception {
        ShiftInputDTO invalidShift = BuilderShiftDTO.builder()
                .datum(datum).startUur(startUur).eindUur(eindUur).maxBaristas(maxBaristas).vestigingId(vestigingId)
                .build().toDTO();

        mockMvc.perform(post("/shift/1/bewerken").flashAttr("shiftInputDTO", invalidShift))
                .andExpect(status().isOk())
                .andExpect(view().name("shift-form"))
                .andExpect(model().attributeHasFieldErrors("shiftInputDTO", expectedErrors));

        verify(mockService, never()).updateShift(any(), any());
    }

    // verwijderen shift post request
    @Test
    void testPostVerwijderen_metVestiging() throws Exception {
        when(mockService.deleteShift(1L)).thenReturn(OK_VESTIGING_ID);

        mockMvc.perform(post("/shift/1/verwijderen"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vestiging/" + OK_VESTIGING_ID));

        verify(mockService).deleteShift(1L);
    }

}
