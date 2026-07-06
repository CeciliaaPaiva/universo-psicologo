package br.com.unipsi.plantao.dto;

import jakarta.validation.constraints.NotNull;

public record AtivarDisponibilidadeRequest(@NotNull Boolean ativo) {
}
