package br.com.unipsi.usuario.dto;

public record AnamneseResponse(
        boolean preenchida,
        Boolean jaFezTerapia,
        String motivoBusca,
        String medicacaoControlada,
        String contatoResponsavel,
        boolean exigeContatoResponsavel) {
}
