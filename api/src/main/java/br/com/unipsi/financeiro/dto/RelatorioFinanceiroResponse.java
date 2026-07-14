package br.com.unipsi.financeiro.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioFinanceiroResponse(
        BigDecimal totalBruto, BigDecimal totalTaxa, BigDecimal totalLiquido, List<CobrancaResponse> cobrancas) {
}
