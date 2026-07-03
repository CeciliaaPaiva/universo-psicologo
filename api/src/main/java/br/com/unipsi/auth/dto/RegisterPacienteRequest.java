package br.com.unipsi.auth.dto;

import br.com.unipsi.usuario.domain.FaixaRenda;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterPacienteRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String senha,
        @NotNull FaixaRenda faixaRenda) {
}
