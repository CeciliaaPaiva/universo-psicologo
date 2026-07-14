package br.com.unipsi.usuario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Faixa de renda não é editável pelo paciente (revertido em 07/07/2026 — ver
 * atas/2026-07-07-alinhamento-sprint-4.md). É autodeclarada uma única vez no cadastro; só pode
 * ser alterada pelo fluxo de revisão de perfil financeiro (US-017/US-027).
 */
public record AtualizarPerfilPacienteRequest(@NotBlank String nome, @Min(0) Integer idade) {
}
