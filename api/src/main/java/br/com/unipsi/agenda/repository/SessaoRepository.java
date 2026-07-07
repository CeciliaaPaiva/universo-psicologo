package br.com.unipsi.agenda.repository;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.StatusSessao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {

    List<Sessao> findByPacienteIdOrderByCriadaEmDesc(UUID pacienteId);

    Optional<Sessao> findBySlotIdAndStatus(UUID slotId, StatusSessao status);
}
