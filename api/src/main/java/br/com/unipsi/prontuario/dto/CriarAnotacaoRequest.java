package br.com.unipsi.prontuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarAnotacaoRequest(@NotBlank @Size(max = 10000) String conteudo) {
}
