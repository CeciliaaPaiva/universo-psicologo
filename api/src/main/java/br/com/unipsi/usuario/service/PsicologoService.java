package br.com.unipsi.usuario.service;

import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.dto.AtualizarPerfilPsicologoRequest;
import br.com.unipsi.usuario.dto.PerfilPsicologoResponse;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PsicologoService {

    private final PsicologoRepository psicologoRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public PerfilPsicologoResponse buscarPerfil(UUID psicologoId) {
        return PerfilPsicologoResponse.from(buscarPsicologo(psicologoId));
    }

    @Transactional
    public PerfilPsicologoResponse atualizarPerfil(
            UUID psicologoId, AtualizarPerfilPsicologoRequest dados, MultipartFile foto) {
        Psicologo psicologo = buscarPsicologo(psicologoId);

        psicologo.setEspecializacao(dados.especializacao());
        psicologo.setPoliticaCancelamento(dados.politicaCancelamento());
        psicologo.setLinkVideochamada(dados.linkVideochamada());
        if (dados.areasAtuacao() != null) {
            psicologo.setAreasAtuacao(dados.areasAtuacao());
        }

        if (foto != null && !foto.isEmpty()) {
            psicologo.setFotoUrl(minioService.enviarFoto(psicologoId, foto));
        }

        return PerfilPsicologoResponse.from(psicologoRepository.save(psicologo));
    }

    private Psicologo buscarPsicologo(UUID psicologoId) {
        return psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));
    }
}
