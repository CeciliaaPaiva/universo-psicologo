package br.com.unipsi.chatbot.service;

import br.com.unipsi.chatbot.domain.ChatMessage;
import br.com.unipsi.chatbot.domain.StatusTriagem;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP direto para a API do Gemini (sem SDK oficial — mesmo padrão do
 * {@code GoogleCalendarService} e {@code ResendClient}). Sem GEMINI_API_KEY configurada, ou em
 * qualquer falha de rede/API, degrada para uma resposta local — nunca deixa a conversa sem
 * resposta. O fallback local ainda faz uma checagem de crise na última mensagem do usuário
 * (reaproveitando {@link CriseDetectorService}), para que o pipeline de plantão continue
 * funcionando mesmo com o Gemini indisponível.
 */
@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String MODELO = "gemini-flash-latest";

    private static final String FALLBACK_CRISE = """
            Sinto muito que você esteja passando por isso — o que você está sentindo é válido e você não está \
            sozinho(a). Vamos tentar uma técnica de respiração agora: inspire contando até 4, segure o ar \
            contando até 7 e solte contando até 8 (respiração 4-7-8). Estou buscando um psicólogo disponível \
            agora para falar com você.""";

    private static final String FALLBACK_GENERICO = """
            Estou com uma instabilidade técnica no momento, mas quero continuar te ouvindo. Pode me contar um \
            pouco mais sobre como você está se sentindo? Se precisar de ajuda imediata, o CVV (188) está \
            disponível 24h.""";

    private final RestClient restClient = RestClient.create("https://generativelanguage.googleapis.com");
    private final CriseDetectorService criseDetectorService;
    private final String apiKey;
    private final String systemPrompt;

    public GeminiClient(
            @Value("${unipsi.gemini.api-key}") String apiKey, CriseDetectorService criseDetectorService) {
        this.apiKey = apiKey;
        this.criseDetectorService = criseDetectorService;
        this.systemPrompt = carregarSystemPrompt();
    }

    public boolean configurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String gerarResposta(List<ChatMessage> historico) {
        if (configurado()) {
            try {
                return chamarApi(historico);
            } catch (Exception e) {
                log.warn("Falha ao chamar a API do Gemini: {}", e.getMessage());
            }
        } else {
            log.debug("GEMINI_API_KEY não configurada — usando resposta de fallback local");
        }
        return gerarFallback(historico);
    }

    @SuppressWarnings("unchecked")
    private String chamarApi(List<ChatMessage> historico) {
        List<Map<String, Object>> contents = historico.stream()
                .map(m -> Map.<String, Object>of("role", m.role(), "parts", List.of(Map.of("text", m.conteudo()))))
                .toList();

        Map<String, Object> corpo = Map.of(
                "contents", contents,
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));

        Map<String, Object> resposta = restClient
                .post()
                .uri("/v1beta/models/{modelo}:generateContent?key={key}", MODELO, apiKey)
                .body(corpo)
                .retrieve()
                .body(Map.class);

        String texto = extrairTexto(resposta);
        if (texto == null || texto.isBlank()) {
            throw new IllegalStateException("Resposta do Gemini veio vazia");
        }
        return texto;
    }

    @SuppressWarnings("unchecked")
    private String extrairTexto(Map<String, Object> resposta) {
        if (resposta == null) {
            return null;
        }
        var candidates = (List<Map<String, Object>>) resposta.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        var content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            return null;
        }
        var parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return (String) parts.get(0).get("text");
    }

    private String gerarFallback(List<ChatMessage> historico) {
        String ultimaMensagemUsuario = ultimaMensagemDoUsuario(historico);
        if (ultimaMensagemUsuario != null
                && criseDetectorService.classificar(ultimaMensagemUsuario) == StatusTriagem.CRISE) {
            return FALLBACK_CRISE;
        }
        return FALLBACK_GENERICO;
    }

    private String ultimaMensagemDoUsuario(List<ChatMessage> historico) {
        for (int i = historico.size() - 1; i >= 0; i--) {
            if ("user".equals(historico.get(i).role())) {
                return historico.get(i).conteudo();
            }
        }
        return null;
    }

    private String carregarSystemPrompt() {
        try {
            return new ClassPathResource("prompts/system_prompt.txt").getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível carregar o system prompt do chatbot", e);
        }
    }
}
