package br.com.unipsi.usuario.dto;

public record AnamnesePsicologoResponse(
        boolean preenchida,
        Boolean jaFezTerapia,
        String motivoBusca,
        String medicacaoControlada,
        String contatoResponsavel,
        boolean menorDeIdade) {
}
