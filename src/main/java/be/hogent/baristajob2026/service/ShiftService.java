package be.hogent.baristajob2026.service;

import be.hogent.baristajob2026.dto.request.ShiftInputDTO;
import be.hogent.baristajob2026.dto.response.ShiftOverviewDTO;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.exception.ShiftNotFoundException;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import be.hogent.baristajob2026.model.Barista;
import be.hogent.baristajob2026.model.Shift;
import be.hogent.baristajob2026.model.Vestiging;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.repository.ShiftRepository;
import be.hogent.baristajob2026.repository.VestigingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftService {

    // maximaal 5 shifts per week per barista
    private static final int MAX_SHIFTS_PER_WEEK = 5;
    // een barista mag zich niet inschrijven voor meer dan 30 uur per week
    private static final int MAX_UREN_PER_WEEK = 30;


    private final ShiftRepository shiftRepository;
    private final VestigingRepository vestigingRepository; // nodig voor shift toevoegen/wijzigen
    private final BaristaRepository baristaRepository; // nodig voor inschrijven

    // convert to DTO methoden
    private ShiftOverviewDTO toOverviewDTO(Shift s) {
        return new ShiftOverviewDTO(
                s.getId(),
                s.getDatum(),
                s.getStartUur(),
                s.getEindUur(),
                s.getRol().name(),
                s.getVestiging() != null ? s.getVestiging().getNaam() : null,
                s.getIngeschrevenBaristas().size(),
                s.getMaxBaristas()
        );
    }

    // voor shift toevoegen/wijzigen form
    public ShiftInputDTO toInputDTO(Shift s) {
        return new ShiftInputDTO(
                s.getId(),
                s.getDatum(),
                s.getStartUur(),
                s.getEindUur(),
                s.getRol(),
                s.getMaxBaristas(),
                s.getVestiging() != null ? s.getVestiging().getId() : null
        );
    }

    // getX methoden

    // voor shiftpagina van een barista
    // toekomstige, niet-volzette shifts in de eigen vestiging waarvoor hij/zij nog niet is ingeschreven
    public List<ShiftOverviewDTO> findBeschikbaarVoorBarista(Long baristaId) {
        Barista barista = baristaRepository.findById(baristaId)
                .orElseThrow(() -> new BaristaNotFoundException(baristaId));

        // zonder vestiging kan een barista geen beschikbare shifts hebben
        // voorkomt ook nullpointer bij barista.getVestiging().getId() in de query hieronder
        if (barista.getVestiging() == null) {
            return List.of();
        }

        LocalDate vandaag = LocalDate.now();

        return shiftRepository.findByVestigingId(barista.getVestiging().getId()).stream()
                .filter(s -> !s.getDatum().isBefore(vandaag)) // enkel toekomstige shifts
                .filter(s -> s.getIngeschrevenBaristas().size() < s.getMaxBaristas()) // nog niet volzet
                .filter(s -> !s.getIngeschrevenBaristas().contains(barista)) // niet reeds ingeschreven
                .sorted(Comparator.comparing(Shift::getDatum))
                .map(this::toOverviewDTO)
                .toList();
    }

    // voor shiftpagina van een barista: eigen (toekomstige) shifts
    public List<ShiftOverviewDTO> findEigenShiften(Long baristaId) {
        Barista barista = baristaRepository.findById(baristaId)
                .orElseThrow(() -> new BaristaNotFoundException(baristaId));

        LocalDate vandaag = LocalDate.now();

        return barista.getShifts().stream()
                .filter(s -> !s.getDatum().isBefore(vandaag))
                .sorted(Comparator.comparing(Shift::getDatum))
                .map(this::toOverviewDTO)
                .toList();
    }

    // voor GET /shift/{id}/bewerken (bewerken van een bestaande shift) - de bestaande shift data wordt in de form gezet
    public ShiftInputDTO findInputById(Long id) {
        Shift s = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));
        return toInputDTO(s);
    }

    // voor POST /shift/nieuw (shift aanmaken)
    public void createShift(ShiftInputDTO dto) {
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        Shift shift = Shift.builder()
                .datum(dto.datum())
                .startUur(dto.startUur())
                .eindUur(dto.eindUur())
                .rol(dto.rol())
                .maxBaristas(dto.maxBaristas())
                .vestiging(vestiging)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        shiftRepository.save(shift);
    }

    // voor POST /shift/{id}/bewerken
    public void updateShift(Long id, ShiftInputDTO dto) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));
        Vestiging vestiging = vestigingRepository.findById(dto.vestigingId())
                .orElseThrow(() -> new VestigingNotFoundException(dto.vestigingId()));

        shift.setDatum(dto.datum());
        shift.setStartUur(dto.startUur());
        shift.setEindUur(dto.eindUur());
        shift.setRol(dto.rol());
        shift.setMaxBaristas(dto.maxBaristas());
        shift.setVestiging(vestiging);

        shiftRepository.save(shift);
    }

    // voor POST /shift/{id}/verwijderen - geeft de vestigingId terug zodat de
    // controller na verwijderen terug naar de juiste vestigingpagina kan sturen
    public Long deleteShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ShiftNotFoundException(id));
        Long vestigingId = shift.getVestiging() != null ? shift.getVestiging().getId() : null;
        shiftRepository.delete(shift);
        return vestigingId;
    }

    // voor POST /barista/{baristaId}/shifts/{shiftId}/inschrijven
    // alle bedrijfsregels rond inschrijven staan hier gebundeld
    public String inschrijven(Long shiftId, Long baristaId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ShiftNotFoundException(shiftId));
        Barista barista = baristaRepository.findById(baristaId)
                .orElseThrow(() -> new BaristaNotFoundException(baristaId));

        // er is geen form object om errors te binden (BindingResult), hier doen we gewoon een inschrijf
        // poging en returnen een foutmelding als er iets mis is, anders null
        // het is geen form submission dus we hoeven geen (custom) annotations/validators te gebruiken

        // additionele businessregel; geblokkeerde baristas mogen zich niet inschrijven
        if (barista.isAdministratiefGeblokkeerd()) {
            return "Je hebt nog openstaande administratieve problemen en kan je niet inschrijven voor shifts.";
        }

        if (shift.getIngeschrevenBaristas().contains(barista)) {
            return "Je bent al ingeschreven voor deze shift.";
        }

        // per vestiging mag een shift maximaal X barista's bevatten
        if (shift.getIngeschrevenBaristas().size() >= shift.getMaxBaristas()) {
            return "Deze shift is volzet.";
        }

        // Een barista mag geen overlappende shifts hebben
        boolean overlapt = barista.getShifts().stream()
                .anyMatch(bestaande -> overlapt(bestaande, shift));
        if (overlapt) {
            return "Deze shift overlapt met een shift waarvoor je al bent ingeschreven.";
        }

        // Maximaal 5 shifts per week per barista
        long shiftsInZelfdeWeek = barista.getShifts().stream()
                .filter(bestaande -> zelfdeWeek(bestaande.getDatum(), shift.getDatum()))
                .count();
        if (shiftsInZelfdeWeek + 1 > MAX_SHIFTS_PER_WEEK) {
            return "Je mag maximaal %d shifts per week hebben.".formatted(MAX_SHIFTS_PER_WEEK);
        }

        // Een barista mag zich niet inschrijven voor meer dan 30 uur per week
        long minutenInZelfdeWeek = barista.getShifts().stream()
                .filter(bestaande -> zelfdeWeek(bestaande.getDatum(), shift.getDatum()))
                .mapToLong(this::duurInMinuten)
                .sum();
        if (minutenInZelfdeWeek + duurInMinuten(shift) > MAX_UREN_PER_WEEK * 60) {
            return "Je mag maximaal %d uur per week inschrijven.".formatted(MAX_UREN_PER_WEEK);
        }

        // Barista is de owning side van de relatie, dus we mogen hier gewoon toevoegen en saven
        barista.getShifts().add(shift);
        baristaRepository.save(barista);
        return null;
    }

    // helper methoden voor de bedrijfsregels hierboven

    private boolean overlapt(Shift a, Shift b) {
        return a.getDatum().equals(b.getDatum())
                && a.getStartUur().isBefore(b.getEindUur())
                && b.getStartUur().isBefore(a.getEindUur());
    }

    private boolean zelfdeWeek(LocalDate d1, LocalDate d2) {
        WeekFields weekFields = WeekFields.ISO;
        return d1.get(weekFields.weekBasedYear()) == d2.get(weekFields.weekBasedYear())
                && d1.get(weekFields.weekOfWeekBasedYear()) == d2.get(weekFields.weekOfWeekBasedYear());
    }

    private long duurInMinuten(Shift s) {
        return Duration.between(s.getStartUur(), s.getEindUur()).toMinutes();
    }

    // voor REST: aantal beschikbare shifts voor een barista
    // hergebruikt findBeschikbaarVoorBarista(), dus zelfde regels (eigen vestiging,
    // toekomstig, niet volzet, niet al ingeschreven) en zelfde BaristaNotFoundException
    public int countBeschikbaarVoorBarista(Long baristaId) {
        return findBeschikbaarVoorBarista(baristaId).size();
    }
}
