package br.com.unipsi.prontuario.dto;

import br.com.unipsi.prontuario.domain.Prontuario;
import java.time.Instant;
import java.util.UUID;

public record ProntuarioResponse(UUID id, String codinome, Instant criadoEm, long totalAnotacoes) {

    public static ProntuarioResponse from(Prontuario prontuario, long totalAnotacoes) {
        return new ProntuarioResponse(
                prontuario.getId(), prontuario.getCodinome(), prontuario.getCriadoEm(), totalAnotacoes);
    }
}
