package be.hogent.baristajob2026.repository;

import be.hogent.baristajob2026.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // voor beschikbare shifts op de shiftpagina van een barista (enkel eigen vestiging)
    List<Shift> findByVestigingId(Long vestigingId);

}
