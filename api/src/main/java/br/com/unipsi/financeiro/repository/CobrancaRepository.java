package br.com.unipsi.financeiro.repository;

import br.com.unipsi.financeiro.domain.Cobranca;
import br.com.unipsi.financeiro.domain.StatusCobranca;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CobrancaRepository extends JpaRepository<Cobranca, UUID> {

    List<Cobranca> findBySessaoPacienteIdOrderByCriadaEmDesc(UUID pacienteId);

    Optional<Cobranca> findBySessaoId(UUID sessaoId);

    @Query("""
            select c from Cobranca c
            where c.sessao.psicologo.id = :psicologoId
            and c.status = :status
            and c.pagaEm between :inicio and :fim
            order by c.pagaEm desc
            """)
    List<Cobranca> buscarPagasPorPsicologoNoPeriodo(
            UUID psicologoId, StatusCobranca status, Instant inicio, Instant fim);

    @Query("""
            select case when count(c) > 0 then true else false end from Cobranca c
            where c.sessao.paciente.id = :pacienteId
            and c.sessao.psicologo.id = :psicologoId
            and c.status = :status
            """)
    boolean existsPagaEntrePacienteEPsicologo(UUID pacienteId, UUID psicologoId, StatusCobranca status);

    @Query("""
            select distinct c.sessao.psicologo.id from Cobranca c
            where c.sessao.paciente.id = :pacienteId and c.status = :status
            """)
    List<UUID> buscarPsicologosComCobrancaPaga(UUID pacienteId, StatusCobranca status);

    @Query("""
            select distinct c.sessao.paciente.id from Cobranca c
            where c.sessao.psicologo.id = :psicologoId and c.status = :status
            """)
    List<UUID> buscarPacientesComCobrancaPaga(UUID psicologoId, StatusCobranca status);
}
