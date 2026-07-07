package br.com.unipsi.notificacao.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ResendClient resendClient;

    public void enviarConfirmacaoCadastroPsicologo(String email, String nome) {
        resendClient.enviar(email, "Cadastro recebido — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Recebemos seu cadastro como psicólogo na plataforma Universo Psicólogo.</p>
                <p>Seu status atual é <strong>PENDENTE</strong>. Nossa equipe irá avaliar seu currículo,
                CRP e política de cancelamento em breve.</p>
                """.formatted(nome));
    }

    public void enviarAprovacaoPsicologo(String email, String nome) {
        resendClient.enviar(email, "Cadastro aprovado — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Seu cadastro foi <strong>aprovado</strong>. Você já pode acessar a plataforma
                e configurar sua agenda.</p>
                """.formatted(nome));
    }

    public void enviarReprovacaoPsicologo(String email, String nome, String motivo) {
        resendClient.enviar(email, "Cadastro não aprovado — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Seu cadastro não foi aprovado neste momento.</p>
                <p><strong>Motivo:</strong> %s</p>
                """.formatted(nome, motivo));
    }

    public void enviarSolicitacaoComplementacao(String email, String nome, String motivo) {
        resendClient.enviar(email, "Complementação de cadastro — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Precisamos de mais informações para avaliar seu cadastro.</p>
                <p><strong>Detalhes:</strong> %s</p>
                """.formatted(nome, motivo));
    }

    public void enviarConfirmacaoAgendamentoPaciente(
            String email, String nomePaciente, String nomePsicologo, LocalDateTime inicio, String linkVideochamada) {
        resendClient.enviar(email, "Sessão agendada — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Sua sessão com <strong>%s</strong> foi agendada para <strong>%s</strong>.</p>
                %s
                """.formatted(nomePaciente, nomePsicologo, inicio.format(FORMATO_DATA_HORA), linkVideochamadaHtml(linkVideochamada)));
    }

    public void enviarConfirmacaoAgendamentoPsicologo(
            String email, String nomePsicologo, String nomePaciente, LocalDateTime inicio, String linkVideochamada) {
        resendClient.enviar(email, "Nova sessão agendada — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Você tem uma nova sessão agendada com <strong>%s</strong> para <strong>%s</strong>.</p>
                %s
                """.formatted(nomePsicologo, nomePaciente, inicio.format(FORMATO_DATA_HORA), linkVideochamadaHtml(linkVideochamada)));
    }

    public void enviarCancelamentoSessao(
            String email, String nomePaciente, String nomePsicologo, LocalDateTime inicio, String motivo) {
        resendClient.enviar(email, "Sessão cancelada — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>Sua sessão com <strong>%s</strong> em <strong>%s</strong> foi cancelada pelo psicólogo.</p>
                %s
                """.formatted(
                nomePaciente,
                nomePsicologo,
                inicio.format(FORMATO_DATA_HORA),
                (motivo != null && !motivo.isBlank()) ? "<p><strong>Motivo:</strong> %s</p>".formatted(motivo) : ""));
    }

    public void enviarAlertaPlantao(String email, String nomePsicologo, String contatoInformado) {
        resendClient.enviar(email, "🔴 Plantão acionado — Universo Psicólogo", """
                <p>Olá, %s!</p>
                <p>O chatbot de triagem identificou uma situação de crise e você está de plantão hoje.</p>
                <p><strong>Horário do acionamento:</strong> %s</p>
                <p><strong>Contato informado pelo visitante:</strong> %s</p>
                <p>Entre em contato o quanto antes, se possível.</p>
                """.formatted(
                nomePsicologo,
                LocalDateTime.now().format(FORMATO_DATA_HORA),
                (contatoInformado != null && !contatoInformado.isBlank()) ? contatoInformado : "não informado"));
    }

    private String linkVideochamadaHtml(String linkVideochamada) {
        return (linkVideochamada != null && !linkVideochamada.isBlank())
                ? "<p>Link da videochamada: <a href=\"%s\">%s</a></p>".formatted(linkVideochamada, linkVideochamada)
                : "";
    }
}
