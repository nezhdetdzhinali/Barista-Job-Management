package be.hogent.baristajob2026.repository;

import be.hogent.baristajob2026.model.Barista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BaristaRepository extends JpaRepository<Barista, Long> {

    // om de inherited loadByUsername() van UserDetailsService te kunnen implementeren
    // volgens de opgave werken we wel met e-mail en geen username
    Optional<Barista> findByEmail (String email);

    // voor overzicht scherm "aantal actieve baristas"
    // wordt gebruikt in de service van Vestiging
    long countByVestigingIdAndActiefTrue(Long vestigingId);

    // voor form barista toevoegen/wijzigen - de validate van unique email en studentenkaartnummer
    // wordt gebruikt in BaristaUniekValidator
    Optional<Barista> findByStudentenkaartNummer(String studentenkaartNummer);

    // voor BaristaStadLimietValidator - aantal actieve baristas tellen per stad (over alle vestigingen in die stad)
    // dit is een join, Barosta heeft een vestiging veld, en een Vestiging object heeft een stad veld
    // zonder de underscore zou Spring zoeken voor een veld "vestigingStad" in Barista en falen
    long countByVestiging_StadAndActiefTrue(String stad);

    // zelfde, maar exclusief de barista die je zelf aan het bewerken bent
    // (anders zou een reeds-actieve barista in die stad zichzelf altijd blokkeren bij bewerken)
    long countByVestiging_StadAndActiefTrueAndIdNot(String stad, Long id);

    // voor REST: GET /api/baristas?stad=
    List<Barista> findByVestiging_Stad(String stad);
}
