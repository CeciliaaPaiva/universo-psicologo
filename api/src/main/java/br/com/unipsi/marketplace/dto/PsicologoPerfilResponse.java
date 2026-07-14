package br.com.unipsi.marketplace.dto;

import br.com.unipsi.agenda.dto.SlotResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PsicologoPerfilResponse(
        UUID id,
        String nome,
        String especializacao,
        List<String> areasAtuacao,
        String politicaCancelamento,
        String linkVideochamada,
        String fotoUrl,
        BigDecimal valorAvulsa,
        BigDecimal valorPacotePorSessao,
        List<SlotResponse> slotsDisponiveis) {
}
