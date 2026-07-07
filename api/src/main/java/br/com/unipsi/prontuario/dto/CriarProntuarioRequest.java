package br.com.unipsi.prontuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarProntuarioRequest(@NotBlank @Size(max = 100) String codinome) {
}
