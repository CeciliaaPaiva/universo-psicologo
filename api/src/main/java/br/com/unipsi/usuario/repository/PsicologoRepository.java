package br.com.unipsi.usuario.repository;

import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PsicologoRepository extends JpaRepository<Psicologo, UUID> {

    List<Psicologo> findByStatusAprovacao(StatusAprovacao statusAprovacao);
}
