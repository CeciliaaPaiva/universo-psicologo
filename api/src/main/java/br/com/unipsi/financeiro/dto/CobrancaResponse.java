package br.com.unipsi.financeiro.dto;

import br.com.unipsi.financeiro.domain.Cobranca;
import br.com.unipsi.financeiro.domain.StatusCobranca;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CobrancaResponse(
        UUID id,
        UUID sessaoId,
        String nomePaciente,
        String nomePsicologo,
        LocalDateTime dataSessao,
        BigDecimal valorBruto,
        BigDecimal taxaPlataforma,
        BigDecimal valorLiquido,
        StatusCobranca status,
        Instant criadaEm,
        Instant pagaEm) {

    public static CobrancaResponse from(Cobranca cobranca) {
        var sessao = cobranca.getSessao();
        return new CobrancaResponse(
                cobranca.getId(),
                sessao.getId(),
                sessao.getPaciente().getUsuario().getNome(),
                sessao.getPsicologo().getUsuario().getNome(),
                sessao.getSlot().getInicio(),
                cobranca.getValorBruto(),
                cobranca.getTaxaPlataforma(),
                cobranca.getValorLiquido(),
                cobranca.getStatus(),
                cobranca.getCriadaEm(),
                cobranca.getPagaEm());
    }
}
