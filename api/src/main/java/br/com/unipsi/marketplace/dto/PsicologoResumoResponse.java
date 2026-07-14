package br.com.unipsi.marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PsicologoResumoResponse(
        UUID id,
        String nome,
        String especializacao,
        List<String> areasAtuacao,
        String fotoUrl,
        List<LocalDateTime> proximasDisponibilidades,
        BigDecimal valorAvulsa,
        BigDecimal valorPacotePorSessao) {
}
