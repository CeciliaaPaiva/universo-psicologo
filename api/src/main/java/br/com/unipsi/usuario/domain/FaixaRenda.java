package br.com.unipsi.usuario.domain;

/**
 * Faixas de renda per capita atendidas pela plataforma (até Classe D).
 * FORA_DO_ESCOPO representa renda acima de Classe D — paciente inelegível.
 */
public enum FaixaRenda {
    FAIXA_1,
    FAIXA_2,
    FAIXA_3,
    FAIXA_4,
    FORA_DO_ESCOPO
}
