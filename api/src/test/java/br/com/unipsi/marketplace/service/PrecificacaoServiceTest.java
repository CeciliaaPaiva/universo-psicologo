package br.com.unipsi.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PrecificacaoServiceTest {

    private final PrecificacaoService precificacaoService = new PrecificacaoService(20);

    @ParameterizedTest
    @CsvSource({
        "FAIXA_1, AVULSA, 60.00",
        "FAIXA_2, AVULSA, 65.00",
        "FAIXA_3, AVULSA, 70.00",
        "FAIXA_4, AVULSA, 75.00",
        "FAIXA_1, PACOTE_MENSAL, 57.00",
        "FAIXA_2, PACOTE_MENSAL, 61.75",
        "FAIXA_3, PACOTE_MENSAL, 66.50",
        "FAIXA_4, PACOTE_MENSAL, 71.25",
    })
    void calcularValorSessao_deveRetornarValorCorretoPorFaixaEModalidade(
            FaixaRenda faixaRenda, Modalidade modalidade, String valorEsperado) {
        BigDecimal valor = precificacaoService.calcularValorSessao(faixaRenda, modalidade);

        assertThat(valor).isEqualByComparingTo(new BigDecimal(valorEsperado));
    }

    @Test
    void calcularValorSessao_avulsaForaDoEscopo_deveLancarPacienteNaoElegivelException() {
        assertThatThrownBy(() -> precificacaoService.calcularValorSessao(FaixaRenda.FORA_DO_ESCOPO, Modalidade.AVULSA))
                .isInstanceOf(PacienteNaoElegivelException.class);
    }

    @Test
    void calcularValorSessao_pacoteForaDoEscopo_deveLancarPacienteNaoElegivelException() {
        assertThatThrownBy(() ->
                        precificacaoService.calcularValorSessao(FaixaRenda.FORA_DO_ESCOPO, Modalidade.PACOTE_MENSAL))
                .isInstanceOf(PacienteNaoElegivelException.class);
    }

    @Test
    void calcularValorSessao_faixaNula_deveLancarPacienteNaoElegivelException() {
        assertThatThrownBy(() -> precificacaoService.calcularValorSessao(null, Modalidade.AVULSA))
                .isInstanceOf(PacienteNaoElegivelException.class);
    }

    @Test
    void calcularTaxa_deveAplicarPercentualConfigurado() {
        BigDecimal taxa = precificacaoService.calcularTaxa(new BigDecimal("60.00"));

        assertThat(taxa).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    void calcularValorLiquido_deveSubtrairTaxaDoValorDaSessao() {
        BigDecimal valorLiquido = precificacaoService.calcularValorLiquido(new BigDecimal("60.00"), new BigDecimal("12.00"));

        assertThat(valorLiquido).isEqualByComparingTo(new BigDecimal("48.00"));
    }
}
