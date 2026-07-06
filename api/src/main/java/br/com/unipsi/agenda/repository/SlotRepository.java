package br.com.unipsi.agenda.repository;

import br.com.unipsi.agenda.domain.Slot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot, UUID> {

    List<Slot> findByPsicologoIdAndInicioBetweenOrderByInicio(
            UUID psicologoId, LocalDateTime inicio, LocalDateTime fim);

    List<Slot> findByPsicologoIdOrderByInicio(UUID psicologoId);

    boolean existsByPsicologoIdAndInicioLessThanAndFimGreaterThan(
            UUID psicologoId, LocalDateTime fim, LocalDateTime inicio);
}
