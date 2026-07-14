package br.com.unipsi.usuario.service;

import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.dto.AtualizarPerfilPacienteRequest;
import br.com.unipsi.usuario.dto.PerfilPacienteResponse;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public PerfilPacienteResponse buscarPerfil(UUID pacienteId) {
        return PerfilPacienteResponse.from(buscarPaciente(pacienteId));
    }

    @Transactional
    public PerfilPacienteResponse atualizarPerfil(
            UUID pacienteId, AtualizarPerfilPacienteRequest dados, MultipartFile foto) {
        Paciente paciente = buscarPaciente(pacienteId);

        paciente.getUsuario().setNome(dados.nome());
        paciente.setIdade(dados.idade());

        if (foto != null && !foto.isEmpty()) {
            paciente.setFotoUrl(minioService.enviarFoto(pacienteId, foto));
        }

        return PerfilPacienteResponse.from(pacienteRepository.save(paciente));
    }

    private Paciente buscarPaciente(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));
    }
}
