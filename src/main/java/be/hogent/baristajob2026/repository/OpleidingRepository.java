package be.hogent.baristajob2026.repository;

import be.hogent.baristajob2026.model.Opleiding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpleidingRepository extends JpaRepository<Opleiding, Long>  {

    // voor REST: GET /api/opleidingen?vestigingId=
    List<Opleiding> findByVestigingId(Long vestigingId);
}
