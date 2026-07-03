package br.com.unipsi.admin.dto;

import br.com.unipsi.admin.domain.DecisaoAprovacao;
import jakarta.validation.constraints.NotNull;

public record DecisaoAprovacaoRequest(@NotNull DecisaoAprovacao decisao, String motivo) {
}
