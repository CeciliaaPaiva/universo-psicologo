package br.com.unipsi.usuario.repository;

import br.com.unipsi.usuario.domain.AuditoriaAnamnese;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAnamneseRepository extends JpaRepository<AuditoriaAnamnese, UUID> {
}
