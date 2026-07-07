package br.com.unipsi.prontuario.repository;

import br.com.unipsi.prontuario.domain.AuditoriaProntuario;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaProntuarioRepository extends JpaRepository<AuditoriaProntuario, UUID> {
}
