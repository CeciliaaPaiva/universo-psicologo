package br.com.unipsi.agenda.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CriarSlotRequest(@NotNull LocalDateTime inicio, @NotNull LocalDateTime fim) {
}
