package br.com.unipsi.agenda.service;

import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.usuario.domain.Psicologo;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Integração com o Google Calendar via chamadas HTTP diretas (OAuth 2.0 Authorization Code + refresh token).
 * Sem GOOGLE_CLIENT_ID/SECRET configurados, ou sem o psicólogo ter conectado a conta, todas as
 * operações são puladas silenciosamente (log em nível debug) — mesmo padrão de degradação do ResendClient.
 */
@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final String ESCOPO = "https://www.googleapis.com/auth/calendar.events";

    private final RestClient oauthClient = RestClient.create("https://oauth2.googleapis.com");
    private final RestClient calendarClient = RestClient.create("https://www.googleapis.com/calendar/v3");

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleCalendarService(
            @Value("${unipsi.google.client-id}") String clientId,
            @Value("${unipsi.google.client-secret}") String clientSecret,
            @Value("${unipsi.google.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public boolean configurado() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public String gerarUrlAutorizacao(String state) {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&access_type=offline"
                + "&prompt=consent"
                + "&scope=" + URLEncoder.encode(ESCOPO, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public String trocarCodigoPorRefreshToken(String code) {
        Map<String, Object> resposta = oauthClient.post()
                .uri("/token")
                .body(Map.of(
                        "code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class);
        return resposta == null ? null : (String) resposta.get("refresh_token");
    }

    public String criarEvento(Psicologo psicologo, Slot slot) {
        String accessToken = obterAccessToken(psicologo);
        if (accessToken == null) {
            return null;
        }
        try {
            DateTimeFormatter formato = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            Map<String, Object> evento = Map.of(
                    "summary", "Sessão — Universo Psicólogo",
                    "start", Map.of("dateTime", slot.getInicio().format(formato)),
                    "end", Map.of("dateTime", slot.getFim().format(formato)));

            @SuppressWarnings("unchecked")
            Map<String, Object> resposta = calendarClient.post()
                    .uri("/calendars/primary/events")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(evento)
                    .retrieve()
                    .body(Map.class);
            return resposta == null ? null : (String) resposta.get("id");
        } catch (Exception e) {
            log.warn("Falha ao criar evento no Google Calendar para psicólogo {}: {}", psicologo.getId(), e.getMessage());
            return null;
        }
    }

    public void removerEvento(Psicologo psicologo, String googleEventId) {
        if (googleEventId == null) {
            return;
        }
        String accessToken = obterAccessToken(psicologo);
        if (accessToken == null) {
            return;
        }
        try {
            calendarClient.delete()
                    .uri("/calendars/primary/events/{id}", googleEventId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Falha ao remover evento {} do Google Calendar: {}", googleEventId, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String obterAccessToken(Psicologo psicologo) {
        if (!configurado() || psicologo.getGoogleRefreshToken() == null) {
            log.debug("Google Calendar não conectado para o psicólogo {} — sincronização ignorada", psicologo.getId());
            return null;
        }
        try {
            Map<String, Object> resposta = oauthClient.post()
                    .uri("/token")
                    .body(Map.of(
                            "refresh_token", psicologo.getGoogleRefreshToken(),
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "grant_type", "refresh_token"))
                    .retrieve()
                    .body(Map.class);
            return resposta == null ? null : (String) resposta.get("access_token");
        } catch (Exception e) {
            log.warn("Falha ao renovar access token do Google para psicólogo {}: {}", psicologo.getId(), e.getMessage());
            return null;
        }
    }
}
