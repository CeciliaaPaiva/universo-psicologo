package br.com.unipsi.agenda.dto;

import br.com.unipsi.agenda.domain.Modalidade;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AgendarSessaoRequest(@NotNull UUID slotId, @NotNull Modalidade modalidade) {
}
