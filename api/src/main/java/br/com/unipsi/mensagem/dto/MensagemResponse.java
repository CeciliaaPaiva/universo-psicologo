package br.com.unipsi.mensagem.dto;

import br.com.unipsi.mensagem.domain.Mensagem;
import java.time.Instant;
import java.util.UUID;

public record MensagemResponse(UUID id, UUID remetenteId, String conteudo, boolean lida, Instant criadaEm) {

    public static MensagemResponse from(Mensagem mensagem) {
        return new MensagemResponse(
                mensagem.getId(),
                mensagem.getRemetenteId(),
                mensagem.getConteudo(),
                mensagem.isLida(),
                mensagem.getCriadaEm());
    }
}
