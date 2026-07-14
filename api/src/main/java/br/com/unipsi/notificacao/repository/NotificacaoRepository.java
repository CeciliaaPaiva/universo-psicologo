package br.com.unipsi.notificacao.repository;

import br.com.unipsi.notificacao.domain.Notificacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(UUID usuarioId);

    long countByUsuarioIdAndLidaFalse(UUID usuarioId);
}
