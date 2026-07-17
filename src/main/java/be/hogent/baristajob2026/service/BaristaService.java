package be.hogent.baristajob2026.service;

import be.hogent.baristajob2026.dto.request.BaristaInputDTO;
import be.hogent.baristajob2026.dto.request.FilterBaristaDTO;
import be.hogent.baristajob2026.dto.response.BaristaDetailDTO;
import be.hogent.baristajob2026.dto.response.BaristaOverviewDTO;
import be.hogent.baristajob2026.dto.response.OpleidingOverviewDTO;
import be.hogent.baristajob2026.dto.response.ShiftDetailDTO;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import be.hogent.baristajob2026.model.*;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.repository.VestigingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaristaService {
    private final BaristaRepository repository;
    // nodig voor barista toevoegen/wijzigen
    private final VestigingRepository vestigingRepository;
    private final PasswordEncoder passwordEncoder;

    // convert to DTO methoden

    // voor overzichtsscherm
    public BaristaOverviewDTO toDTO(Barista b) {
        return new BaristaOverviewDTO(
                b.getId(),
                b.getVoornaam(),
                b.getAchternaam(),
                b.getVestiging() != null ? b.getVestiging().getStad() : null, // stad is een berekend gegeven uit entiteit vestiging
                b.isActief(),
                b.getGeboortedatum(),
                b.getShifts().size(), // aantal shifts is een sorteer optie in het overzichtsscherm, dus we tellen hier het aantal shifts
                b.getOpleidings().stream() // nodig voor barista filter optie op opleiding in overzichtsscherm
                        .map(o -> new OpleidingOverviewDTO(o.getId(), o.getTitel()))
                        .toList()
        );
    }

    // voor barista detail pagina
    private BaristaDetailDTO toDetailDTO(Barista b) {
        LocalDate vandaag = LocalDate.now();

        List<ShiftDetailDTO> toekomstigeShifts = b.getShifts().stream()
                .filter(s -> !s.getDatum().isBefore(vandaag)) // houd alleen shifts die vandaag of later zijn (toekomstige shifts)
                .sorted(Comparator.comparing(Shift::getDatum)) // sorteer op datum, van vroeg naar laat
                .map(s -> new ShiftDetailDTO(s.getId(),s.getDatum(), s.getStartUur(), s.getEindUur()))
                .toList();

        List<ShiftDetailDTO> afgelopenShifts = b.getShifts().stream()
                .filter(s -> s.getDatum().isBefore(vandaag)) // houd alleen shifts die in het verleden liggen (afgelopen shifts)
                .sorted(Comparator.comparing(Shift::getDatum).reversed()) // sorteer op datum, van laat naar vroeg (meest recente afgelopen shift eerst)
                .map(s -> new ShiftDetailDTO(s.getId(),s.getDatum(), s.getStartUur(), s.getEindUur()))
                .toList();

        // splitsing gebeurt op entity-niveau (o.getDatum()), maar we mappen naar de
        // al bestaande OpleidingOverviewDTO - geen nieuwe DTO nodig
        List<OpleidingOverviewDTO> geplandeOpleidingen = b.getOpleidings().stream()
                .filter(o -> !o.getDatum().isBefore(vandaag)) // houd alleen opleidingen die vandaag of later zijn (geplande opleidingen)
                .sorted(Comparator.comparing(Opleiding::getDatum)) // sorteer op datum, van vroeg naar laat (eerstvolgende opleiding eerst)
                .map(o -> new OpleidingOverviewDTO(o.getId(), o.getTitel()))
                .toList();

        List<OpleidingOverviewDTO> voltooideOpleidingen = b.getOpleidings().stream()
                .filter(o -> o.getDatum().isBefore(vandaag)) // houd alleen opleidingen die in het verleden liggen (voltooide opleidingen)
                .sorted(Comparator.comparing(Opleiding::getDatum).reversed()) // sorteer op datum, van laat naar vroeg (meest recent voltooide opleiding eerst)
                .map(o -> new OpleidingOverviewDTO(o.getId(), o.getTitel()))
                .toList();

        return new BaristaDetailDTO(
                b.getId(), b.getVoornaam(), b.getAchternaam(), b.getEmail(),
                b.getGeboortedatum(), b.getStudentenkaartNummer(), b.isActief(),
                b.getVestiging() != null ? b.getVestiging().getNaam() : null,
                toekomstigeShifts, afgelopenShifts, geplandeOpleidingen, voltooideOpleidingen
        );
    }

    // voor barista toevoegen/wijzigen form
    public BaristaInputDTO toInputDTO(Barista b) {
        return new BaristaInputDTO(
                b.getId(),
                b.getVoornaam(),
                b.getAchternaam(),
                b.getEmail(),
                b.getGeboortedatum(),
                b.getStudentenkaartNummer(),
                b.isActief(),
                b.getVestiging() != null ? b.getVestiging().getId() : null
        );
    }


    // getX methoden

    // voor spring secruity, we werken met email ipv username
    public Barista findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Barista not found: ".formatted(email)));
    }

    // voor overzichtsscherm - toon alle baristas
    public List<BaristaOverviewDTO> findAllOverview() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // voor barista filters in overzichtsscherm
    public List<BaristaOverviewDTO> findFilteredBaristas(FilterBaristaDTO filter) {
        return repository.findAll().stream()
                // filter op city
                .filter(b -> filter.stadSelected() == null || filter.stadSelected().isBlank()
                        || (b.getVestiging() != null && filter.stadSelected().equals(b.getVestiging().getStad())))
                // filter op status
                .filter(b -> filter.actiefSelected() == null || filter.actiefSelected().isBlank()
                        ||
                        // filter.actiefSelected() bevat "true" of "false" als string vanuit de form
                        // dus we moeten eerst converteren naar een boolean voordat we vergelijken met b.isActief()
                        b.isActief() == Boolean.parseBoolean(filter.actiefSelected()))
                // filter op opleiding
                .filter(b -> filter.opleidingSelected() == null
                        ||
                        // een barista heeft een lijst van opleidingen
                        // dus je streamt zijn data set van opleidingen, en kijkt
                        // of er een match is met de geselecteerde opleiding
                        b.getOpleidings().stream().anyMatch(o -> o.getId().equals(filter.opleidingSelected())))
                // sorteer op naam, aantal shifts of geboortedatum
                // filter.sortSelected() bevat de gekozen sorteeroptie als string vanuit de form
                .sorted((b1, b2) -> {
                    if (filter.sortSelected() == null || filter.sortSelected().isBlank()) {
                        return 0; // geen sortering
                    }
                    return switch (filter.sortSelected()) {
                        case "naam" -> b1.getVoornaam().compareTo(b2.getVoornaam());
                        case "shifts" -> Integer.compare(b1.getShifts().size(), b2.getShifts().size());
                        case "geboortedatum" -> b1.getGeboortedatum().compareTo(b2.getGeboortedatum());
                        default -> 0;
                    };
                })
                .map(this::toDTO)
                .toList();
    }

    // voor barista detail pagina
    // controller geeft enkel de email en admin-status van de ingelogde gebruiker door
    // hier wordt effectief BESLIST of die persoon dit profiel mag zien
    public BaristaDetailDTO findDetailById(Long id, String currentUserEmail, boolean isAdmin) {
        Barista b = repository.findById(id)
                .orElseThrow(() -> new BaristaNotFoundException(id));

        // autorisatieregel: enkel admins en de barista zelf mogen dit profiel zien
        if (!isAdmin && !b.getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Geen toegang tot dit profiel");
        }

        return toDetailDTO(b);
    }

    // voor GET /barista/{id}/bewerken
    public BaristaInputDTO findInputById(Long id) {
        Barista b = repository.findById(id)
                .orElseThrow(() -> new BaristaNotFoundException(id));
        return toInputDTO(b);
    }

    // voor POST /barista/nieuw
    public void createBarista(BaristaInputDTO dto) {
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        Barista barista = Barista.builder()
                .voornaam(dto.voornaam())
                .achternaam(dto.achternaam())
                .email(dto.email())
                .geboortedatum(dto.geboortedatum())
                .studentenkaartNummer(dto.studentenkaartNummer())
                .actief(dto.actief() != null && dto.actief())
                .administratiefGeblokkeerd(false)
                .vestiging(vestiging)
                // er is geen wachtwoordveld in het formulier
                // dus we geven een tijdelijk wachtwoord op basis van het studentenkaartnummer
                .password(passwordEncoder.encode(dto.studentenkaartNummer()))
                .rol(Rol.BARISTA)
                .shiftRol(ShiftRol.BARISTA)
                .shifts(new HashSet<>())
                .opleidings(new HashSet<>())
                .build();

        repository.save(barista);
    }

    // voor POST /barista/{id}/bewerken
    public void updateBarista(Long id, BaristaInputDTO dto) {
        Barista barista = repository.findById(id)
                .orElseThrow(() -> new BaristaNotFoundException(id));
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        barista.setVoornaam(dto.voornaam());
        barista.setAchternaam(dto.achternaam());
        barista.setEmail(dto.email());
        barista.setGeboortedatum(dto.geboortedatum());
        barista.setStudentenkaartNummer(dto.studentenkaartNummer());
        barista.setActief(dto.actief() != null && dto.actief());
        barista.setVestiging(vestiging);

        repository.save(barista);
    }

    // voor de "Mijn shifts" link in de navbar
    // /barista/2/shifts -> id nodig om shift pagina van de ingelogde gebruiker te tonen
    public Long findIdByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new BaristaNotFoundException(email))
                .getId();
    }

    // voor BaristaUniekValidator - checkt of een andere barista al dit email gebruikt
    public boolean bestaatAndereBaristaMetEmail(String email, Long id) {
        return repository.findByEmail(email)
                .filter(b -> !b.getId().equals(id))
                .isPresent();
    }

    // voor BaristaUniekValidator - checkt of een andere barista al dit studentenkaartnummer gebruikt
    public boolean bestaatAndereBaristaMetStudentenkaart(String studentenkaartNummer, Long id) {
        return repository.findByStudentenkaartNummer(studentenkaartNummer)
                .filter(b -> !b.getId().equals(id))
                .isPresent();
    }

    // voor BaristaStadLimietValidator - aantal actieve baristas in een stad,
    // exclusief de barista die je zelf aan het bewerken bent (excludeId == null bij aanmaken)
    public long telActieveBaristasInStad(String stad, Long excludeId) {
        return excludeId != null
                ? repository.countByVestiging_StadAndActiefTrueAndIdNot(stad, excludeId)
                : repository.countByVestiging_StadAndActiefTrue(stad);
    }


    // voor REST: baristas in een gegeven stad
    public List<BaristaOverviewDTO> findByStad(String stad) {
        return repository.findByVestiging_Stad(stad).stream()
                .map(this::toDTO)
                .toList();
    }
}

