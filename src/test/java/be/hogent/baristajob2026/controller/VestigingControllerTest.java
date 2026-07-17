package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import be.hogent.baristajob2026.dto.response.VestigingDetailDTO;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import be.hogent.baristajob2026.init.BuilderVestigingDTO;
import be.hogent.baristajob2026.service.BaristaService;
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

import java.util.stream.Stream;

import static be.hogent.baristajob2026.init.InitVestiging.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VestigingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({VestigingUniekValidator.class, BaristaUniekValidator.class, BaristaStadLimietValidator.class})
public class VestigingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VestigingService mockService;

    @MockitoBean
    private BaristaService baristaService;

    // get request detail pagina vestiging
    @Test
    void testGetDetail() throws Exception {
        VestigingDetailDTO expected = new VestigingDetailDTO(1L, OK_NAAM, OK_STAD, OK_AANTAL_ZITPLAATSEN, java.util.List.of(), java.util.List.of(), java.util.List.of());
        when(mockService.findDetailById(1L)).thenReturn(expected);

        mockMvc.perform(get("/vestiging/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("vestiging-detail"))
                .andExpect(model().attributeExists("vestiging"))
                .andExpect(model().attribute("vestiging", expected));

        verify(mockService).findDetailById(1L);
    }

    // get request vestiging detail pagina - niet gevonden
    @Test
    void testGetDetail_notFound() throws Exception {
        when(mockService.findDetailById(1L)).thenThrow(new VestigingNotFoundException(1L));

        mockMvc.perform(get("/vestiging/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));
    }

    // get request vestiging detail pagina - ongeldig id formaat
    @Test
    void testInvalidIdFormat_detail() throws Exception {
        mockMvc.perform(get("/vestiging/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(mockService, never()).findDetailById(any());
    }

    // get request nieuw vestiging form
    @Test
    void testGetNieuwForm() throws Exception {
        mockMvc.perform(get("/vestiging/nieuw"))
                .andExpect(status().isOk())
                .andExpect(view().name("vestiging-form"))
                .andExpect(model().attributeExists("vestigingInputDTO"));
    }


    // post request nieuw vestiging form - geldig
    @Test
    void testPostNieuwForm_valid() throws Exception {
        doNothing().when(mockService).createVestiging(any(VestigingInputDTO.class));

        VestigingInputDTO validVestiging = BuilderVestigingDTO.builder().build().toDTO();

        mockMvc.perform(post("/vestiging/nieuw").flashAttr("vestigingInputDTO", validVestiging))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));

        verify(mockService).createVestiging(any());
    }

    private static Stream<Arguments> invalidVestigingData() {
        return Stream.of(
                Arguments.of(null, OK_STAD, OK_AANTAL_ZITPLAATSEN, new String[]{"naam"}),
                Arguments.of("", OK_STAD, OK_AANTAL_ZITPLAATSEN, new String[]{"naam"}),
                Arguments.of(OK_NAAM, null, OK_AANTAL_ZITPLAATSEN, new String[]{"stad"}),
                Arguments.of(OK_NAAM, OK_STAD, null, new String[]{"aantalZitplaatsen"}),
                Arguments.of(OK_NAAM, OK_STAD, 0, new String[]{"aantalZitplaatsen"}),
                Arguments.of(null, "", 0, new String[]{"naam", "stad", "aantalZitplaatsen"})
        );
    }


    @ParameterizedTest
    @MethodSource("invalidVestigingData")
    void testPostNieuwForm_invalid(String naam, String stad, Integer aantalZitplaatsen, String[] expectedErrors) throws Exception {
        VestigingInputDTO invalidVestiging = BuilderVestigingDTO.builder()
                .naam(naam).stad(stad).aantalZitplaatsen(aantalZitplaatsen).build().toDTO();

        mockMvc.perform(post("/vestiging/nieuw").flashAttr("vestigingInputDTO", invalidVestiging))
                .andExpect(status().isOk())
                .andExpect(view().name("vestiging-form"))
                .andExpect(model().attributeHasFieldErrors("vestigingInputDTO", expectedErrors));

        verify(mockService, never()).createVestiging(any());
    }

    // get request bewerken form
    @Test
    void testGetBewerkenForm() throws Exception {
        VestigingInputDTO expected = BuilderVestigingDTO.builder().build().toDTO();
        when(mockService.findInputById(1L)).thenReturn(expected);

        mockMvc.perform(get("/vestiging/1/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("vestiging-form"))
                .andExpect(model().attributeExists("vestigingInputDTO"))
                .andExpect(model().attribute("vestigingInputDTO", expected));

        verify(mockService).findInputById(1L);
    }

    // get request bewerken form - vestiging niet gevonden
    @Test
    void testGetBewerkenForm_notFound() throws Exception {
        when(mockService.findInputById(1L)).thenThrow(new VestigingNotFoundException(1L));

        mockMvc.perform(get("/vestiging/1/bewerken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/overview"));
    }

    // get request bewerken form - ongeldig id formaat
    @Test
    void testInvalidIdFormat_bewerken() throws Exception {
        mockMvc.perform(get("/vestiging/abc/bewerken"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));

        verify(mockService, never()).findInputById(any());
    }

    // post request bewerken form - geldig
    @Test
    void testPostBewerkenForm_valid() throws Exception {
        doNothing().when(mockService).updateVestiging(eq(1L), any(VestigingInputDTO.class));

        VestigingInputDTO validVestiging = BuilderVestigingDTO.builder().build().toDTO();

        mockMvc.perform(post("/vestiging/1/bewerken").flashAttr("vestigingInputDTO", validVestiging))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vestiging/1"));

        verify(mockService).updateVestiging(eq(1L), any());
    }

    // post request bewerken form - ongeldig
    @ParameterizedTest
    @MethodSource("invalidVestigingData")
    void testPostBewerkenForm_invalid(String naam, String stad, Integer aantalZitplaatsen, String[] expectedErrors) throws Exception {
        VestigingInputDTO invalidVestiging = BuilderVestigingDTO.builder()
                .naam(naam).stad(stad).aantalZitplaatsen(aantalZitplaatsen).build().toDTO();

        mockMvc.perform(post("/vestiging/1/bewerken").flashAttr("vestigingInputDTO", invalidVestiging))
                .andExpect(status().isOk())
                .andExpect(view().name("vestiging-form"))
                .andExpect(model().attributeHasFieldErrors("vestigingInputDTO", expectedErrors));

        verify(mockService, never()).updateVestiging(any(), any());
    }
}
