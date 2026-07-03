package br.com.unipsi.notificacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

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
}
