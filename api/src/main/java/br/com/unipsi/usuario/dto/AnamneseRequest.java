package br.com.unipsi.usuario.dto;

import jakarta.validation.constraints.NotNull;

/**
 * {@code contatoResponsavel} só é obrigatório quando o paciente é menor de idade
 * (ver {@code Paciente.isMenorDeIdade()}) — validado em {@code AnamneseService}.
 */
public record AnamneseRequest(
        @NotNull Boolean jaFezTerapia, String motivoBusca, String medicacaoControlada, String contatoResponsavel) {
}
