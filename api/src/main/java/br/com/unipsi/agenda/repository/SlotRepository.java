package br.com.unipsi.agenda.repository;

import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot, UUID> {

    List<Slot> findByPsicologoIdAndInicioBetweenOrderByInicio(
            UUID psicologoId, LocalDateTime inicio, LocalDateTime fim);

    List<Slot> findByPsicologoIdOrderByInicio(UUID psicologoId);

    List<Slot> findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(UUID psicologoId, LocalDateTime apos);

    boolean existsByPsicologoIdAndInicioLessThanAndFimGreaterThan(
            UUID psicologoId, LocalDateTime fim, LocalDateTime inicio);

    /**
     * Usado pelo chatbot em situação de crise (US-019, ajuste 07/07/2026): quando não há ninguém
     * de plantão hoje, busca o psicólogo aprovado com a próxima disponibilidade mais próxima na
     * agenda (não restrito a quem está de plantão).
     */
    Optional<Slot> findFirstByDisponivelTrueAndInicioAfterAndPsicologoStatusAprovacaoOrderByInicioAsc(
            LocalDateTime apos, StatusAprovacao statusAprovacao);
}
