package br.com.unipsi.prontuario.repository;

import br.com.unipsi.prontuario.domain.Prontuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {

    List<Prontuario> findByPsicologoIdOrderByCriadoEmDesc(UUID psicologoId);

    Optional<Prontuario> findByPsicologoIdAndCodinome(UUID psicologoId, String codinome);

    boolean existsByPsicologoIdAndCodinome(UUID psicologoId, String codinome);
}
