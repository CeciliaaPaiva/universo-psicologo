package br.com.unipsi.notificacao.service;

import br.com.unipsi.notificacao.domain.Notificacao;
import br.com.unipsi.notificacao.dto.NotificacaoResponse;
import br.com.unipsi.notificacao.repository.NotificacaoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    @Transactional
    public void criar(UUID usuarioId, String mensagem) {
        notificacaoRepository.save(Notificacao.builder()
                .usuarioId(usuarioId)
                .mensagem(mensagem)
                .lida(false)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(UUID usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId).stream()
                .map(NotificacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(UUID usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarLida(UUID usuarioId, UUID notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        if (!notificacao.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Notificação não encontrada");
        }
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasLidas(UUID usuarioId) {
        notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId).stream()
                .filter(n -> !n.isLida())
                .forEach(n -> n.setLida(true));
    }
}
