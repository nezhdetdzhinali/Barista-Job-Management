package be.hogent.baristajob2026.service;


import be.hogent.baristajob2026.dto.request.VestigingInputDTO;
import be.hogent.baristajob2026.dto.response.*;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import be.hogent.baristajob2026.model.Shift;
import be.hogent.baristajob2026.model.Vestiging;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.repository.VestigingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VestigingService {
    private final VestigingRepository vestigingRepository;
    // we injecteren ook be baristaRepository omdat we daar de query hebben om
    // het aantal actieve baristas te tellen per vestiging
    private final BaristaRepository baristaRepository;
    // hergebruik van de bestaande DTO-mappings ipv ze hier te dupliceren (voor detail pagina vestiging met baristas, shifts en opleidingen)
    private final BaristaService baristaService;
    private final OpleidingService opleidingService;


    // convert to DTO methoden
    // voor overzichtsscherm hebben we een DTO nodig die ook het aantal actieve baristas bevat
    private VestigingOverviewDTO toDTO(Vestiging v) {

        long activeBaristas = baristaRepository
                .countByVestigingIdAndActiefTrue(v.getId());

        return new VestigingOverviewDTO(
                v.getId(),
                v.getNaam(),
                v.getStad(),
                v.getAantalZitplaatsen(),
                activeBaristas
        );
    }

    // voor vestiging detailpagina - baristas, shifts en opleidingen van deze vestiging
    private VestigingDetailDTO toDetailDTO(Vestiging v) {

        List<BaristaOverviewDTO> baristas = v.getBaristas().stream()
                .map(baristaService::toDTO) // hergebruik van BaristaService's mapping
                .toList();

        List<ShiftDetailDTO> shifts = v.getShifts().stream()
                .sorted(Comparator.comparing(Shift::getDatum)) // sorteer op datum, van vroeg naar laat
                .map(s -> new ShiftDetailDTO(s.getId(),s.getDatum(), s.getStartUur(), s.getEindUur()))
                .toList();

        List<OpleidingOverviewDTO> opleidingen = v.getOpleidingen().stream()
                .map(opleidingService::toDTO) // hergebruik van OpleidingService's mapping
                .toList();

        return new VestigingDetailDTO(
                v.getId(),
                v.getNaam(),
                v.getStad(),
                v.getAantalZitplaatsen(),
                baristas,
                shifts,
                opleidingen
        );
    }

    // voor barista filters in overzichtsscherm hebben we een DTO nodig die alleen de stad bevat
    private StadDTO toStadDTO(String stadsNaam) {
        return new StadDTO(stadsNaam);
    }

    // getX methoden

    // voor overzichtsscherm - toon alle vestigingen
    public List<VestigingOverviewDTO> findAll() {
        return vestigingRepository.findAll()
                .stream()
                .map(this::toDTO) // active baristas zitten inbegrepen in de DTO
                .toList();
    }

    public List<StadDTO> getAllSteden() {
        return vestigingRepository.findAll()
                .stream()
                .map(Vestiging::getStad)
                .filter(stad -> stad != null && !stad.isBlank())
                .distinct()
                .sorted()
                .map(this::toStadDTO)
                .toList();
    }

    // voor vestiging detail pagina
    // geen autorisatieregel nodig zoals bij barista: vestiging-info is voor iedereen zichtbaar,
    // de RBAC op baristagegevens gebeurt in de view (sec:authorize), net zoals in overview.html
    public VestigingDetailDTO findDetailById(Long id) {
        Vestiging v = vestigingRepository.findById(id)
                .orElseThrow(() -> new VestigingNotFoundException(id));

        return toDetailDTO(v);
    }

    // voor vestiging toevoegen/wijzigen form
    public VestigingInputDTO toInputDTO(Vestiging v) {
        return new VestigingInputDTO(v.getId(), v.getNaam(), v.getStad(), v.getAantalZitplaatsen());
    }

    // voor GET /vestiging/{id}/bewerken
    public VestigingInputDTO findInputById(Long id) {
        Vestiging v = vestigingRepository.findById(id)
                .orElseThrow(() -> new VestigingNotFoundException(id));
        return toInputDTO(v);
    }

    // voor POST /vestiging/nieuw
    public void createVestiging(VestigingInputDTO dto) {
        Vestiging vestiging = Vestiging.builder()
                .naam(dto.naam())
                .stad(dto.stad())
                .aantalZitplaatsen(dto.aantalZitplaatsen())
                .baristas(new ArrayList<>())
                .shifts(new ArrayList<>())
                .opleidingen(new ArrayList<>())
                .build();
        vestigingRepository.save(vestiging);
    }

    // voor POST /vestiging/{id}/bewerken
    public void updateVestiging(Long id, VestigingInputDTO dto) {
        Vestiging vestiging = vestigingRepository.findById(id)
                .orElseThrow(() -> new VestigingNotFoundException(id));
        vestiging.setNaam(dto.naam());
        vestiging.setStad(dto.stad());
        vestiging.setAantalZitplaatsen(dto.aantalZitplaatsen());
        vestigingRepository.save(vestiging);
    }

    // voor BaristaStadLimietValidator - de stad opzoeken a.d.h.v. de vestigingId uit het formulier
    // null indien de vestiging niet bestaat (validator slaat de check dan gewoon over)
    public String findStadById(Long vestigingId) {
        return vestigingRepository.findById(vestigingId)
                .map(Vestiging::getStad)
                .orElse(null);
    }

    // voor VestigingUniekValidator - checkt of een andere vestiging al deze naam+stad combinatie gebruikt
    public boolean bestaatAndereVestigingMetNaamEnStad(String naam, String stad, Long id) {
        return vestigingRepository.findByNaamAndStad(naam, stad)
                .filter(v -> !v.getId().equals(id))
                .isPresent();
    }
}