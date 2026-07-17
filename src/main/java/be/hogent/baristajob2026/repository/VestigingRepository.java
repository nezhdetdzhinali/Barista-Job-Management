package be.hogent.baristajob2026.repository;

import be.hogent.baristajob2026.model.Vestiging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VestigingRepository extends JpaRepository<Vestiging, Long> {

    // voor VestigingUniekValidator - geen twee vestigingen met dezelfde combinatie naam + stad
    Optional<Vestiging> findByNaamAndStad(String naam, String stad);
}

