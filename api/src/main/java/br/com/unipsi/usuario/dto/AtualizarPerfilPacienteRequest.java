package br.com.unipsi.usuario.dto;

import br.com.unipsi.usuario.domain.FaixaRenda;
import jakarta.validation.constraints.NotNull;

public record AtualizarPerfilPacienteRequest(@NotNull FaixaRenda faixaRenda) {
}
