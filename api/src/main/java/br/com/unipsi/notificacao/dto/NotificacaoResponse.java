package br.com.unipsi.notificacao.dto;

import br.com.unipsi.notificacao.domain.Notificacao;
import java.time.Instant;
import java.util.UUID;

public record NotificacaoResponse(UUID id, String mensagem, boolean lida, Instant criadaEm) {

    public static NotificacaoResponse from(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(), notificacao.getMensagem(), notificacao.isLida(), notificacao.getCriadaEm());
    }
}
