package be.hogent.baristajob2026.config;

import be.hogent.baristajob2026.model.*;
import be.hogent.baristajob2026.repository.BaristaRepository;
import be.hogent.baristajob2026.repository.OpleidingRepository;
import be.hogent.baristajob2026.repository.ShiftRepository;
import be.hogent.baristajob2026.repository.VestigingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class InitDataConfig implements CommandLineRunner {
    private final BaristaRepository baristaRepository;
    private final OpleidingRepository opleidingRepository;
    private final ShiftRepository shiftRepository;
    private final VestigingRepository vestigingRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        // Vestigingen
        Vestiging v1 = Vestiging.builder()
                .naam("Gent Centrum")
                .stad("Gent")
                .aantalZitplaatsen(50)
                .baristas(new ArrayList<>())
                .shifts(new ArrayList<>())
                .opleidingen(new ArrayList<>())
                .build();
        Vestiging v2 = Vestiging.builder()
                .naam("Antwerpen Zuid")
                .stad("Antwerpen")
                .aantalZitplaatsen(40)
                .baristas(new ArrayList<>())
                .shifts(new ArrayList<>())
                .opleidingen(new ArrayList<>())
                .build();
        Vestiging v3 = Vestiging.builder()
                .naam("Brussel Noord")
                .stad("Brussel")
                .aantalZitplaatsen(60)
                .baristas(new ArrayList<>())
                .shifts(new ArrayList<>())
                .opleidingen(new ArrayList<>())
                .build();

        vestigingRepository.save(v1);
        vestigingRepository.save(v2);
        vestigingRepository.save(v3);

        // Opleidingen - elk 1 in het verleden (voltooid) en 1 in de toekomst (gepland)
        Opleiding o1 = Opleiding.builder()
                .titel("Intro Espresso")
                .beschrijving("Basistraining voor espresso en machine")
                .datum(LocalDate.now().minusDays(10)) // voltooid
                .duurInUur(3)
                .maxDeelnemers(20)
                .vestiging(v1)
                .deelnemers(new HashSet<>())
                .build();

        Opleiding o2 = Opleiding.builder()
                .titel("Latte Art Basics")
                .beschrijving("Melkopschuimen en eenvoudige latte art")
                .datum(LocalDate.now().plusDays(14)) // gepland
                .duurInUur(2)
                .maxDeelnemers(12)
                .vestiging(v2)
                .deelnemers(new HashSet<>())
                .build();

        Opleiding o3 = Opleiding.builder()
                .titel("Barista Advanced")
                .beschrijving("Advanced brewing techniques en troubleshooting")
                .datum(LocalDate.now().minusDays(3)) // voltooid
                .duurInUur(4)
                .maxDeelnemers(15)
                .vestiging(v3)
                .deelnemers(new HashSet<>())
                .build();

        Opleiding o4 = Opleiding.builder()
                .titel("Cocktails Basics")
                .beschrijving("Introductie tot niet-alcoholische cocktails")
                .datum(LocalDate.now().plusDays(21)) // gepland
                .duurInUur(2)
                .maxDeelnemers(10)
                .vestiging(v1)
                .deelnemers(new HashSet<>())
                .build();

        opleidingRepository.save(o1);
        opleidingRepository.save(o2);
        opleidingRepository.save(o3);
        opleidingRepository.save(o4);

        // Shifts - elk 1 in het verleden (afgelopen) en 1 in de toekomst (toekomstig)
        Shift s1 = Shift.builder()
                .datum(LocalDate.now().plusDays(1)) // toekomstig
                .startUur(LocalTime.of(9, 0))
                .eindUur(LocalTime.of(13, 0))
                .rol(ShiftRol.BARISTA)
                .maxBaristas(3)
                .vestiging(v1)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        Shift s2 = Shift.builder()
                .datum(LocalDate.now().minusDays(5)) // afgelopen
                .startUur(LocalTime.of(13, 0))
                .eindUur(LocalTime.of(17, 0))
                .rol(ShiftRol.KASSIER)
                .maxBaristas(2)
                .vestiging(v2)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        Shift s3 = Shift.builder()
                .datum(LocalDate.now().plusDays(3)) // toekomstig
                .startUur(LocalTime.of(17, 0))
                .eindUur(LocalTime.of(21, 0))
                .rol(ShiftRol.BARISTA)
                .maxBaristas(4)
                .vestiging(v3)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        Shift s4 = Shift.builder()
                .datum(LocalDate.now().minusDays(2)) // afgelopen
                .startUur(LocalTime.of(9, 0))
                .eindUur(LocalTime.of(12, 0))
                .rol(ShiftRol.KASSIER)
                .maxBaristas(3)
                .vestiging(v1)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        Shift s5 = Shift.builder()
                .datum(LocalDate.now().plusDays(2)) // toekomstig, nog niet volzet, niemand ingeschreven
                .startUur(LocalTime.of(10, 0))
                .eindUur(LocalTime.of(14, 0))
                .rol(ShiftRol.BARISTA)
                .maxBaristas(2)
                .vestiging(v1)
                .ingeschrevenBaristas(new HashSet<>())
                .build();

        shiftRepository.save(s1);
        shiftRepository.save(s2);
        shiftRepository.save(s3);
        shiftRepository.save(s4);
        shiftRepository.save(s5);
        // Baristas (3) - elk gekoppeld aan 1 afgelopen + 1 toekomstige shift, en 1 voltooide + 1 geplande opleiding
        Barista b1 = Barista.builder()
                .studentenkaartNummer("20261001")
                .voornaam("Jan")
                .achternaam("Janssens")
                .email("jan.janssens@hogent.be")
                .password(passwordEncoder.encode("12345678"))
                .geboortedatum(LocalDate.of(1999, 4, 12))
                .actief(true)
                .administratiefGeblokkeerd(false)
                .vestiging(v1)
                .shiftRol(ShiftRol.BARISTA)
                .rol(Rol.ADMIN) // security role - admin
                .shifts(new HashSet<>(Arrays.asList(s1, s4)))
                .opleidings(new HashSet<>(Arrays.asList(o1, o4)))
                .build();

        Barista b2 = Barista.builder()
                .studentenkaartNummer("20261002")
                .voornaam("Lies")
                .achternaam("Peeters")
                .email("lies.peeters@hogent.be")
                .password(passwordEncoder.encode("12345678"))
                .geboortedatum(LocalDate.of(2000, 2, 20))
                .actief(true)
                .administratiefGeblokkeerd(false)
                .vestiging(v2)
                .rol(Rol.BARISTA) // gewoone user
                .shiftRol(ShiftRol.KASSIER)
                .shifts(new HashSet<>(Arrays.asList(s2, s3)))
                .opleidings(new HashSet<>(Arrays.asList(o2, o3)))
                .build();

        Barista b3 = Barista.builder()
                .studentenkaartNummer("20261003")
                .voornaam("Tom")
                .achternaam("De Vries")
                .email("tom.devries@hogent.be")
                .password(passwordEncoder.encode("12345678"))
                .geboortedatum(LocalDate.of(1998, 11, 5))
                .actief(true)
                .administratiefGeblokkeerd(false)
                .vestiging(v3)
                .shiftRol(ShiftRol.BARISTA)
                .rol(Rol.BARISTA) // gewoone user
                .shifts(new HashSet<>(Arrays.asList(s3, s4)))
                .opleidings(new HashSet<>(Arrays.asList(o3, o4)))
                .build();

        baristaRepository.save(b1);
        baristaRepository.save(b2);
        baristaRepository.save(b3);
    }
}