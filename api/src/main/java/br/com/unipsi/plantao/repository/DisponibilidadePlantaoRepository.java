package br.com.unipsi.plantao.repository;

import br.com.unipsi.plantao.domain.DisponibilidadePlantao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DisponibilidadePlantaoRepository extends JpaRepository<DisponibilidadePlantao, UUID> {

    List<DisponibilidadePlantao> findByPsicologoIdOrderByCriadoEmDesc(UUID psicologoId);

    /**
     * Carrega psicologo.usuario via JOIN FETCH: o resultado é consumido por
     * {@code PlantaoService.buscarPsicologosDePlantaoHoje()} fora de uma transação mais ampla
     * (chamado a partir do ChatbotService, que não é @Transactional), então a associação lazy
     * precisa vir inicializada — sem isso, o acesso a psicologo.getUsuario() fora da sessão do
     * Hibernate lançaria LazyInitializationException.
     */
    @Query("SELECT d FROM DisponibilidadePlantao d JOIN FETCH d.psicologo p JOIN FETCH p.usuario WHERE d.ativo = true")
    List<DisponibilidadePlantao> findByAtivoTrue();
}
