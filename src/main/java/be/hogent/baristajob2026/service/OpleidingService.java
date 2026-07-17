package be.hogent.baristajob2026.service;

import be.hogent.baristajob2026.dto.request.FilterOpleidingDTO;
import be.hogent.baristajob2026.dto.request.OpleidingInputDTO;
import be.hogent.baristajob2026.dto.response.OpleidingDetailDTO;
import be.hogent.baristajob2026.dto.response.OpleidingOverviewDTO;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.exception.OpleidingNotFoundException;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import be.hogent.baristajob2026.model.Barista;
import be.hogent.baristajob2026.model.Opleiding;
import be.hogent.baristajob2026.model.Vestiging;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.repository.OpleidingRepository;
import be.hogent.baristajob2026.repository.VestigingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpleidingService {

    // "binnen de actieve periode" is zelf te kiezen (cfr. opgave) -> we tellen enkel
    // nog-niet-plaatsgevonden (geplande) opleidingen mee, analoog aan de geplande/voltooide-
    // opsplitsing die al bestaat in BaristaService.toDetailDTO
    private static final int MAX_OPLEIDINGEN_ACTIEF = 3;


    private final OpleidingRepository repository;
    private final BaristaRepository baristaRepository; // nodig voor inschrijven
    private final VestigingRepository vestigingRepository; // nodig voor opleiding toevoegen/wijzigen

    // convert to DTO methoden

    // voor barista/vestiging detailpaginas
    public OpleidingOverviewDTO toDTO(Opleiding o) {
        return new OpleidingOverviewDTO(
                o.getId(),
                o.getTitel()
        );
    }

    // voor het opleidingsscherm
    // currentBaristaId is null voor anonieme bezoekers en admins -> alReedsIngeschreven blijft dan false
    private OpleidingDetailDTO toDetailDTO(Opleiding o, Long currentBaristaId) {
        boolean alReedsIngeschreven = currentBaristaId != null
                && o.getDeelnemers().stream().anyMatch(b -> b.getId().equals(currentBaristaId));

        return new OpleidingDetailDTO(
                o.getId(),
                o.getTitel(),
                o.getBeschrijving(),
                o.getDatum(),
                o.getDuurInUur(),
                o.getMaxDeelnemers(),
                o.getDeelnemers().size(),
                o.getVestiging() != null ? o.getVestiging().getId() : null,
                o.getVestiging() != null ? o.getVestiging().getNaam() : null,
                alReedsIngeschreven,
                o.getDeelnemers().stream()
                        .map(b -> b.getVoornaam() + " " + b.getAchternaam())
                        .toList()
        );
    }

    // voor opleiding toevoegen/wijzigen form
    private OpleidingInputDTO toInputDTO(Opleiding o) {
        return new OpleidingInputDTO(
                o.getId(),
                o.getTitel(),
                o.getBeschrijving(),
                o.getDatum(),
                o.getDuurInUur(),
                o.getMaxDeelnemers(),
                o.getVestiging() != null ? o.getVestiging().getId() : null
        );
    }


    // getX methoden
    public List<OpleidingOverviewDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // voor opleidingsscherm - gefilterde lijst van alle opleidingen
    // currentBaristaId komt van de controller (null bij anonieme bezoekers/admins)
    public List<OpleidingDetailDTO> findFilteredOpleidingen(FilterOpleidingDTO filter, Long currentBaristaId) {
        return repository.findAll().stream()
                // filter op vestiging
                .filter(o -> filter.vestigingSelected() == null
                        || (o.getVestiging() != null && filter.vestigingSelected().equals(o.getVestiging().getId())))
                // filter op titel (contains, case-insensitive)
                .filter(o -> filter.titelSelected() == null || filter.titelSelected().isBlank()
                        || o.getTitel().toLowerCase().contains(filter.titelSelected().toLowerCase()))
                // filter op beschikbaarheid
                .filter(o -> {
                    if (filter.beschikbaarheidSelected() == null || filter.beschikbaarheidSelected().isBlank()) {
                        return true; // geen filter
                    }
                    boolean beschikbaar = o.getDeelnemers().size() < o.getMaxDeelnemers();
                    return switch (filter.beschikbaarheidSelected()) {
                        case "beschikbaar" -> beschikbaar;
                        case "volgeboekt" -> !beschikbaar;
                        default -> true;
                    };
                })
                .sorted(Comparator.comparing(Opleiding::getDatum))
                .map(o -> toDetailDTO(o, currentBaristaId))
                .toList();
    }

    // opleiding inschrijven met businessregels
    public String inschrijven(Long opleidingId, Long baristaId) {
        Opleiding opleiding = repository.findById(opleidingId)
                .orElseThrow(() -> new OpleidingNotFoundException(opleidingId));
        Barista barista = baristaRepository.findById(baristaId)
                .orElseThrow(() -> new BaristaNotFoundException(baristaId));

        // additionele businessregel; geblokkeerde baristas mogen zich niet inschrijven
        if (barista.isAdministratiefGeblokkeerd()) {
            return "Je hebt nog openstaande administratieve problemen en kan je niet inschrijven voor opleidingen.";
        }

        if (opleiding.getDeelnemers().contains(barista)) {
            return "Je bent al ingeschreven voor deze opleiding.";
        }

        // opleiding mag niet overboeken: deelnemers <= maxDeelnemers
        if (opleiding.getDeelnemers().size() >= opleiding.getMaxDeelnemers()) {
            return "Deze opleiding is volgeboekt.";
        }

        // maximaal 3 opleidingen binnen de actieve (nog niet plaatsgevonden) periode
        LocalDate vandaag = LocalDate.now();
        long geplandeOpleidingen = barista.getOpleidings().stream()
                .filter(o -> !o.getDatum().isBefore(vandaag))
                .count();
        if (geplandeOpleidingen + 1 > MAX_OPLEIDINGEN_ACTIEF) {
            return "Je kan maximaal %d opleidingen tegelijk ingepland hebben.".formatted(MAX_OPLEIDINGEN_ACTIEF);
        }

        // Barista is de owning side van de relatie, dus we mogen hier gewoon toevoegen en saven
        barista.getOpleidings().add(opleiding);
        baristaRepository.save(barista);
        return null;
    }

    // voor GET /opleiding/{id}/bewerken
    public OpleidingInputDTO findInputById(Long id) {
        Opleiding o = repository.findById(id)
                .orElseThrow(() -> new OpleidingNotFoundException(id));
        return toInputDTO(o);
    }

    // voor POST /opleiding/nieuw
    public void createOpleiding(OpleidingInputDTO dto) {
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        Opleiding opleiding = Opleiding.builder()
                .titel(dto.titel())
                .beschrijving(dto.beschrijving())
                .datum(dto.datum())
                .duurInUur(dto.duurInUur())
                .maxDeelnemers(dto.maxDeelnemers())
                .vestiging(vestiging)
                .deelnemers(new HashSet<>())
                .build();

        repository.save(opleiding);
    }

    // voor POST /opleiding/{id}/bewerken
    public void updateOpleiding(Long id, OpleidingInputDTO dto) {
        Opleiding opleiding = repository.findById(id)
                .orElseThrow(() -> new OpleidingNotFoundException(id));
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        opleiding.setTitel(dto.titel());
        opleiding.setBeschrijving(dto.beschrijving());
        opleiding.setDatum(dto.datum());
        opleiding.setDuurInUur(dto.duurInUur());
        opleiding.setMaxDeelnemers(dto.maxDeelnemers());
        opleiding.setVestiging(vestiging);

        repository.save(opleiding);
    }

    // voor REST: opleidingen per vestiging
    public List<OpleidingOverviewDTO> findByVestigingId(Long vestigingId) {
        if (!vestigingRepository.existsById(vestigingId)) {
            throw new VestigingNotFoundException(vestigingId);
        }
        return repository.findByVestigingId(vestigingId).stream()
                .map(this::toDTO)
                .toList();
    }
}
