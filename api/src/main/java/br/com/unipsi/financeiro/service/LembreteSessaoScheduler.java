package br.com.unipsi.financeiro.service;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.notificacao.service.NotificacaoService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Roda a cada 5 minutos verificando sessões que entram na janela de 24h ou 1h antes do início.
 * Janela de +/-5min ao redor do alvo evita reenvio duplicado sem exigir precisão de segundo.
 */
@Component
@RequiredArgsConstructor
public class LembreteSessaoScheduler {

    private static final int JANELA_MINUTOS = 5;
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");

    private final SessaoRepository sessaoRepository;
    private final EmailService emailService;
    private final NotificacaoService notificacaoService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void enviarLembretes() {
        enviarLembretes24h();
        enviarLembretes1h();
    }

    private void enviarLembretes24h() {
        LocalDateTime alvo = LocalDateTime.now().plusHours(24);
        List<Sessao> sessoes = sessaoRepository.findByStatusAndLembrete24hEnviadoFalseAndSlotInicioBetween(
                StatusSessao.AGENDADA, alvo.minusMinutes(JANELA_MINUTOS), alvo.plusMinutes(JANELA_MINUTOS));

        for (Sessao sessao : sessoes) {
            enviarLembrete(sessao);
            sessao.setLembrete24hEnviado(true);
            sessaoRepository.save(sessao);
        }
    }

    private void enviarLembretes1h() {
        LocalDateTime alvo = LocalDateTime.now().plusHours(1);
        List<Sessao> sessoes = sessaoRepository.findByStatusAndLembrete1hEnviadoFalseAndSlotInicioBetween(
                StatusSessao.AGENDADA, alvo.minusMinutes(JANELA_MINUTOS), alvo.plusMinutes(JANELA_MINUTOS));

        for (Sessao sessao : sessoes) {
            enviarLembrete(sessao);
            sessao.setLembrete1hEnviado(true);
            sessaoRepository.save(sessao);
        }
    }

    private void enviarLembrete(Sessao sessao) {
        var paciente = sessao.getPaciente();
        var psicologo = sessao.getPsicologo();
        LocalDateTime inicio = sessao.getSlot().getInicio();

        emailService.enviarLembreteSessao(
                paciente.getUsuario().getEmail(),
                paciente.getUsuario().getNome(),
                psicologo.getUsuario().getNome(),
                inicio,
                psicologo.getLinkVideochamada());

        emailService.enviarLembreteSessao(
                psicologo.getUsuario().getEmail(),
                psicologo.getUsuario().getNome(),
                paciente.getUsuario().getNome(),
                inicio,
                psicologo.getLinkVideochamada());

        String horario = inicio.format(FORMATO_DATA_HORA);
        notificacaoService.criar(paciente.getId(),
                "Lembrete: sua sessão com %s é em %s".formatted(psicologo.getUsuario().getNome(), horario));
        notificacaoService.criar(psicologo.getId(),
                "Lembrete: sua sessão com %s é em %s".formatted(paciente.getUsuario().getNome(), horario));
    }
}
