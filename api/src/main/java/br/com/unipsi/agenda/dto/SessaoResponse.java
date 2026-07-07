package br.com.unipsi.agenda.dto;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.StatusSessao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID id,
        LocalDateTime inicio,
        LocalDateTime fim,
        String nomePsicologo,
        Modalidade modalidade,
        BigDecimal valorSessao,
        StatusSessao status,
        String linkVideochamada) {

    public static SessaoResponse from(Sessao sessao) {
        return new SessaoResponse(
                sessao.getId(),
                sessao.getSlot().getInicio(),
                sessao.getSlot().getFim(),
                sessao.getPsicologo().getUsuario().getNome(),
                sessao.getModalidade(),
                sessao.getValorSessao(),
                sessao.getStatus(),
                sessao.getPsicologo().getLinkVideochamada());
    }
}
