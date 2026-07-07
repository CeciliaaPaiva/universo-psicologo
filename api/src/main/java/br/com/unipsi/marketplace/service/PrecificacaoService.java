package br.com.unipsi.marketplace.service;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Calcula o valor da sessão pela faixa de renda do paciente e a modalidade escolhida.
 * Tabela de valores fixada em CLAUDE.md — "Regras de negócio críticas" (não são configuráveis).
 */
@Service
public class PrecificacaoService {

    private static final String MSG_INELEGIVEL =
            "A plataforma atende exclusivamente pacientes de baixa renda (até Classe D). "
                    + "Procure atendimento particular.";

    private final BigDecimal taxaPlataformaPercentual;

    public PrecificacaoService(@Value("${unipsi.taxa-plataforma-percentual}") int taxaPlataformaPercentual) {
        this.taxaPlataformaPercentual = BigDecimal.valueOf(taxaPlataformaPercentual);
    }

    public BigDecimal calcularValorSessao(FaixaRenda faixaRenda, Modalidade modalidade) {
        if (faixaRenda == null) {
            throw new PacienteNaoElegivelException(MSG_INELEGIVEL);
        }
        return switch (modalidade) {
            case AVULSA -> valorAvulsa(faixaRenda);
            case PACOTE_MENSAL -> valorPacotePorSessao(faixaRenda);
        };
    }

    public BigDecimal calcularTaxa(BigDecimal valorSessao) {
        return valorSessao
                .multiply(taxaPlataformaPercentual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularValorLiquido(BigDecimal valorSessao, BigDecimal taxaPlataforma) {
        return valorSessao.subtract(taxaPlataforma);
    }

    private BigDecimal valorAvulsa(FaixaRenda faixaRenda) {
        return switch (faixaRenda) {
            case FAIXA_1 -> new BigDecimal("60.00");
            case FAIXA_2 -> new BigDecimal("65.00");
            case FAIXA_3 -> new BigDecimal("70.00");
            case FAIXA_4 -> new BigDecimal("75.00");
            case FORA_DO_ESCOPO -> throw new PacienteNaoElegivelException(MSG_INELEGIVEL);
        };
    }

    private BigDecimal valorPacotePorSessao(FaixaRenda faixaRenda) {
        return switch (faixaRenda) {
            case FAIXA_1 -> new BigDecimal("57.00");
            case FAIXA_2 -> new BigDecimal("61.75");
            case FAIXA_3 -> new BigDecimal("66.50");
            case FAIXA_4 -> new BigDecimal("71.25");
            case FORA_DO_ESCOPO -> throw new PacienteNaoElegivelException(MSG_INELEGIVEL);
        };
    }
}
