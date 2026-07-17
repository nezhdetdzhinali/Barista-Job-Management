package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.request.FilterBaristaDTO;
import be.hogent.baristajob2026.dto.response.BaristaOverviewDTO;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.VestigingService;
import be.hogent.baristajob2026.validator.BaristaStadLimietValidator;
import be.hogent.baristajob2026.validator.BaristaUniekValidator;
import be.hogent.baristajob2026.validator.VestigingUniekValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OverviewController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import({BaristaUniekValidator.class, BaristaStadLimietValidator.class, VestigingUniekValidator.class})
public class OverviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VestigingService vestigingService;

    @MockitoBean
    private BaristaService baristaService;

    @MockitoBean
    private OpleidingService opleidingService;

    @Test
    void testGetRequest() throws Exception {
        List<BaristaOverviewDTO> alleBaristas = List.of();
        when(baristaService.findAllOverview()).thenReturn(alleBaristas);

        mockMvc.perform(get("/overview"))
                .andExpect(status().isOk())
                .andExpect(view().name("overview"))
                .andExpect(model().attributeExists("filterBaristaDTO"))
                .andExpect(model().attributeExists("baristas"))
                .andExpect(model().attributeExists("vestigingen"))
                .andExpect(model().attributeExists("stedenList"))
                .andExpect(model().attributeExists("opleidingenList"))
                .andExpect(model().attribute("baristas", alleBaristas));

        verify(baristaService).findAllOverview();
    }

    @Test
    void testPostRequestFilter() throws Exception {
        FilterBaristaDTO filter = new FilterBaristaDTO("Gent", "true", null, "naam");
        List<BaristaOverviewDTO> gefilterdeBaristas = List.of();
        when(baristaService.findFilteredBaristas(filter)).thenReturn(gefilterdeBaristas);

        mockMvc.perform(post("/overview").flashAttr("filterBaristaDTO", filter))
                .andExpect(status().isOk())
                .andExpect(view().name("overview"))
                .andExpect(model().attributeExists("baristas"))
                .andExpect(model().attribute("baristas", gefilterdeBaristas));

        verify(baristaService).findFilteredBaristas(filter);
    }
}
