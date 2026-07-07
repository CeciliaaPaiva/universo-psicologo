package br.com.unipsi.usuario.service;

import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import br.com.unipsi.usuario.dto.AtualizarPerfilPacienteRequest;
import br.com.unipsi.usuario.dto.PerfilPacienteResponse;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    @Transactional(readOnly = true)
    public PerfilPacienteResponse buscarPerfil(UUID pacienteId) {
        return PerfilPacienteResponse.from(buscarPaciente(pacienteId));
    }

    @Transactional
    public PerfilPacienteResponse atualizarPerfil(UUID pacienteId, AtualizarPerfilPacienteRequest dados) {
        if (dados.faixaRenda() == FaixaRenda.FORA_DO_ESCOPO) {
            throw new PacienteNaoElegivelException(
                    "A plataforma atende exclusivamente pacientes de baixa renda (até Classe D). "
                            + "Procure atendimento particular.");
        }

        Paciente paciente = buscarPaciente(pacienteId);
        paciente.setFaixaRenda(dados.faixaRenda());
        return PerfilPacienteResponse.from(pacienteRepository.save(paciente));
    }

    private Paciente buscarPaciente(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));
    }
}
