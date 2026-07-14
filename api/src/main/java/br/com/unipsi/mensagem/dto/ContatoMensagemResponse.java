package br.com.unipsi.mensagem.dto;

import java.util.UUID;

public record ContatoMensagemResponse(UUID id, String nome, long naoLidas) {
}
