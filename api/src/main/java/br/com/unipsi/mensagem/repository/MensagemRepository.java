package br.com.unipsi.mensagem.repository;

import br.com.unipsi.mensagem.domain.Mensagem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemRepository extends JpaRepository<Mensagem, UUID> {

    List<Mensagem> findByPacienteIdAndPsicologoIdOrderByCriadaEmAsc(UUID pacienteId, UUID psicologoId);

    List<Mensagem> findByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
            UUID pacienteId, UUID psicologoId, UUID remetenteId);

    long countByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
            UUID pacienteId, UUID psicologoId, UUID remetenteId);
}
