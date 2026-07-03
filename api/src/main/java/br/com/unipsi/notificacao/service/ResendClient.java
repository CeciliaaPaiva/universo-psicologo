package br.com.unipsi.notificacao.service;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ResendClient {

    private static final Logger log = LoggerFactory.getLogger(ResendClient.class);

    private final RestClient restClient;
    private final String remetente;

    public ResendClient(
            @Value("${unipsi.resend.api-key}") String apiKey,
            @Value("${unipsi.resend.remetente}") String remetente) {
        this.remetente = remetente;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public void enviar(String destinatario, String assunto, String html) {
        try {
            restClient.post()
                    .uri("/emails")
                    .body(Map.of(
                            "from", remetente,
                            "to", List.of(destinatario),
                            "subject", assunto,
                            "html", html))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}
