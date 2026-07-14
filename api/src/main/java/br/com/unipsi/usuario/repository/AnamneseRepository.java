package br.com.unipsi.usuario.repository;

import br.com.unipsi.usuario.domain.Anamnese;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnamneseRepository extends JpaRepository<Anamnese, UUID> {

    Optional<Anamnese> findByPacienteId(UUID pacienteId);
}
