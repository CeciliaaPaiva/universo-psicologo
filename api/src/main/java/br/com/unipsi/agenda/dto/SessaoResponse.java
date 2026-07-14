package br.com.unipsi.agenda.dto;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.domain.TipoAtendimento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID id,
        LocalDateTime inicio,
        LocalDateTime fim,
        UUID psicologoId,
        String nomePsicologo,
        UUID pacienteId,
        String nomePaciente,
        Modalidade modalidade,
        TipoAtendimento tipoAtendimento,
        BigDecimal valorSessao,
        BigDecimal valorSessaoAvulsa,
        BigDecimal valorPacoteTotal,
        BigDecimal economiaPacote,
        StatusSessao status,
        String linkVideochamada) {

    public static SessaoResponse from(
            Sessao sessao, BigDecimal valorSessaoAvulsa, BigDecimal valorPacoteTotal, BigDecimal economiaPacote) {
        return new SessaoResponse(
                sessao.getId(),
                sessao.getSlot().getInicio(),
                sessao.getSlot().getFim(),
                sessao.getPsicologo().getId(),
                sessao.getPsicologo().getUsuario().getNome(),
                sessao.getPaciente().getId(),
                sessao.getPaciente().getUsuario().getNome(),
                sessao.getModalidade(),
                sessao.getTipoAtendimento(),
                sessao.getValorSessao(),
                valorSessaoAvulsa,
                valorPacoteTotal,
                economiaPacote,
                sessao.getStatus(),
                sessao.getPsicologo().getLinkVideochamada());
    }
}
