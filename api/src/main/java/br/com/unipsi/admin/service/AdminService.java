package br.com.unipsi.admin.service;

import br.com.unipsi.admin.dto.DecisaoAprovacaoRequest;
import br.com.unipsi.admin.dto.PsicologoPendenteResponse;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PsicologoRepository psicologoRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<PsicologoPendenteResponse> listarPendentes() {
        return psicologoRepository.findByStatusAprovacao(StatusAprovacao.PENDENTE_APROVACAO).stream()
                .map(PsicologoPendenteResponse::from)
                .toList();
    }

    @Transactional
    public void decidir(UUID psicologoId, DecisaoAprovacaoRequest decisao) {
        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        switch (decisao.decisao()) {
            case APROVAR -> {
                psicologo.setStatusAprovacao(StatusAprovacao.APROVADO);
                psicologo.setMotivoReprovacao(null);
                emailService.enviarAprovacaoPsicologo(
                        psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome());
            }
            case REPROVAR -> {
                if (decisao.motivo() == null || decisao.motivo().isBlank()) {
                    throw new IllegalArgumentException("Motivo é obrigatório para reprovar um cadastro");
                }
                psicologo.setStatusAprovacao(StatusAprovacao.REPROVADO);
                psicologo.setMotivoReprovacao(decisao.motivo());
                emailService.enviarReprovacaoPsicologo(
                        psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome(), decisao.motivo());
            }
            case SOLICITAR_COMPLEMENTACAO -> {
                if (decisao.motivo() == null || decisao.motivo().isBlank()) {
                    throw new IllegalArgumentException("Detalhe a complementação solicitada");
                }
                emailService.enviarSolicitacaoComplementacao(
                        psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome(), decisao.motivo());
            }
        }

        psicologoRepository.save(psicologo);
    }
}
