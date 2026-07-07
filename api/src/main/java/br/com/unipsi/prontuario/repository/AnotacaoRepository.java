package br.com.unipsi.prontuario.repository;

import br.com.unipsi.prontuario.domain.Anotacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnotacaoRepository extends JpaRepository<Anotacao, UUID> {

    List<Anotacao> findByProntuarioIdOrderByCriadaEmDesc(UUID prontuarioId);

    long countByProntuarioId(UUID prontuarioId);
}
