package be.hogent.baristajob2026.controller;

import be.hogent.baristajob2026.dto.response.BaristaDetailDTO;
import be.hogent.baristajob2026.dto.response.VestigingDetailDTO;
import be.hogent.baristajob2026.model.Barista;
import be.hogent.baristajob2026.model.Rol;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.service.BaristaService;
import be.hogent.baristajob2026.service.OpleidingService;
import be.hogent.baristajob2026.service.ShiftService;
import be.hogent.baristajob2026.service.VestigingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import be.hogent.baristajob2026.init.BuilderBaristaDTO;
import be.hogent.baristajob2026.init.BuilderOpleidingDTO;
import be.hogent.baristajob2026.init.BuilderVestigingDTO;
import be.hogent.baristajob2026.init.BuilderShiftDTO;
import be.hogent.baristajob2026.model.ShiftRol;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private VestigingService mockVestigingService;

    @MockitoBean
    private BaristaService mockBaristaService;

    @MockitoBean
    private ShiftService mockShiftService;

    @MockitoBean
    private OpleidingService mockOpleidingService;

    @MockitoBean
    private BaristaRepository mockBaristaRepository;

    @BeforeEach
    void setup() {
        Barista barista = Barista.builder()
                .email("jan.peeters@test.be")
                .password(passwordEncoder.encode("geheim123"))
                .rol(Rol.BARISTA)
                .build();
        when(mockBaristaRepository.findByEmail("jan.peeters@test.be")).thenReturn(Optional.of(barista));
    }


    // login/403 schermen zelf

    @ParameterizedTest
    @CsvSource({"/login, login", "/403, error/403"})
    void testGetViews(String url, String expectedViewName) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));
    }

    // publieke views sectie


    // een guest moet de publieke paginas kunnen bereiken (vestiging detail, overview, opleidingen) met de beperkte data
    @WithAnonymousUser
    @ParameterizedTest
    @CsvSource({
            "/vestiging/1, vestiging-detail",
            "/overview, overview",
            "/opleiding, opleiding-overview"
    })
    void testPermitAllReachableAnonymous(String url, String expectedViewName) throws Exception {
        when(mockVestigingService.findDetailById(any())).thenReturn(
                new VestigingDetailDTO(1L, "Centrum", "Gent", 20, List.of(), List.of(), List.of()));
        when(mockVestigingService.findAll()).thenReturn(List.of()); // nodig voor /vestiging/1 zelf (vestiging-detail)
        when(mockVestigingService.getAllSteden()).thenReturn(List.of()); // nodig voor /overview (de lijst vestigingen op dat scherm)
        when(mockBaristaService.findAllOverview()).thenReturn(List.of()); // nodig voor /overview (steden-filter dropdown)
        when(mockOpleidingService.findAll()).thenReturn(List.of()); //  nodig voor /overview (de baristalijst op dat scherm)
        when(mockOpleidingService.findFilteredOpleidingen(any(), any())).thenReturn(List.of()); // nodig voor /opleiding zelf (opleiding-overview)

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));
    }

    // admin views sectie

//     admin heeft toegang tot CRUD paginas
    @WithMockUser(roles = "ADMIN")
    @ParameterizedTest
    @CsvSource({
            "/vestiging/nieuw, vestiging-form",
            "/vestiging/1/bewerken, vestiging-form",
            "/barista/nieuw, barista-form",
            "/barista/1/bewerken, barista-form",
            "/opleiding/nieuw, opleiding-form",
            "/opleiding/1/bewerken, opleiding-form",
            "/shift/nieuw, shift-form",
            "/shift/1/bewerken, shift-form"
    })
    void testAdminOnlyPermittedForAdmin(String url, String expectedViewName) throws Exception {
        when(mockVestigingService.findInputById(any())).thenReturn(BuilderVestigingDTO.builder().build().toDTO());
        when(mockBaristaService.findInputById(any())).thenReturn(BuilderBaristaDTO.builder().build().toDTO());
        when(mockOpleidingService.findInputById(any())).thenReturn(BuilderOpleidingDTO.builder().build().toDTO());
        when(mockShiftService.findInputById(any())).thenReturn(BuilderShiftDTO.builder().build().toDTO());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));
    }


    // barista heeft geen toegang tot CRUD paginas
    @WithMockUser(roles = "BARISTA")
    @Test
    void testAdminOnlyBlockedForBarista() throws Exception {
        mockMvc.perform(get("/vestiging/nieuw"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/vestiging/1/bewerken"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/barista/nieuw"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/barista/1/bewerken"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/opleiding/nieuw"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/opleiding/1/bewerken"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/shift/nieuw"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/shift/1/bewerken"))
                .andExpect(status().isForbidden());

    }

    // guest heeft geen toegang tot CRUD paginas
    @WithAnonymousUser
    @Test
    void testAdminOnlyBlockedForAnonymous() throws Exception {
        mockMvc.perform(get("/vestiging/nieuw"))
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/vestiging/1/bewerken"))
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/barista/nieuw"))
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/barista/1/bewerken"))
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/opleiding/nieuw"))
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/opleiding/1/bewerken"))
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/shift/nieuw"))
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/shift/1/bewerken"))
                .andExpect(redirectedUrlPattern("**/login"));

    }


    // partiele toegang sectie, hasAnyRole(BARISTA, ADMIN), beide rollen mogen en guest niet

    //  barista kan zijn eigen detail pagina bereikein
    @WithMockUser(username = "barista@test.be", roles = "BARISTA")
    @Test
    void testBaristaOrAdminPermittedForBarista() throws Exception {
        when(mockBaristaService.findDetailById(eq(1L), any(), eq(false))).thenReturn(
                new BaristaDetailDTO(1L, "Jan", "Peeters", "barista@test.be", null,
                        "12345678", true, "Centrum", List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/barista/1"))
                .andExpect(status().isOk());
    }

    // admin kan detail paginas van baristas zien
    @WithMockUser(roles = "ADMIN")
    @Test
    void testBaristaOrAdminPermittedForAdmin() throws Exception {
        when(mockBaristaService.findDetailById(eq(1L), any(), eq(true))).thenReturn(
                new BaristaDetailDTO(1L, "Jan", "Peeters", "j@test.be", null,
                        "12345678", true, "Centrum", List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/barista/1"))
                .andExpect(status().isOk());
    }
    
    // inloggen sectie

    @Test
    void testCorrectLogin() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("email", "jan.peeters@test.be")
                        .password("password", "geheim123"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/overview"));
    }

    @Test
    void testWrongPassword() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("email", "jan.peeters@test.be")
                        .password("password", "foutwachtwoord"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error"));
    }
}