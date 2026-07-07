package br.com.unipsi.prontuario.dto;

import java.time.Instant;
import java.util.UUID;

public record AnotacaoResponse(UUID id, String conteudo, Instant criadaEm) {
}
