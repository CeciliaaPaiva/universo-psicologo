package br.com.unipsi.usuario.repository;

import br.com.unipsi.usuario.domain.Paciente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
}
