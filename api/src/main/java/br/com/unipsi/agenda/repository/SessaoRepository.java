package br.com.unipsi.agenda.repository;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.StatusSessao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    List<Sessao> findByPacienteIdOrderByCriadaEmDesc(UUID pacienteId);

    List<Sessao> findByPsicologoIdOrderByCriadaEmDesc(UUID psicologoId);

    Optional<Sessao> findBySlotIdAndStatus(UUID slotId, StatusSessao status);

    List<Sessao> findByStatusAndLembrete24hEnviadoFalseAndSlotInicioBetween(
            StatusSessao status, LocalDateTime inicio, LocalDateTime fim);

    List<Sessao> findByStatusAndLembrete1hEnviadoFalseAndSlotInicioBetween(
            StatusSessao status, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByPacienteIdAndPsicologoIdAndStatus(UUID pacienteId, UUID psicologoId, StatusSessao status);
}
