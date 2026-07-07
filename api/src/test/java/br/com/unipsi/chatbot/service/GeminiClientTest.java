package br.com.unipsi.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.unipsi.chatbot.domain.ChatMessage;
import br.com.unipsi.chatbot.domain.StatusTriagem;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Sem chave real do Gemini nem infraestrutura de mock de HTTP no projeto (mesmo padrão de
 * ResendClient/GoogleCalendarService, que também não têm teste do caminho de rede real), estes
 * testes cobrem o que é determinístico: detecção de configuração e a geração do fallback local.
 */
class GeminiClientTest {

    private final CriseDetectorService criseDetectorService = new CriseDetectorService();

    @Test
    void configurado_semChave_deveRetornarFalse() {
        GeminiClient geminiClient = new GeminiClient("", criseDetectorService);

        assertThat(geminiClient.configurado()).isFalse();
    }

    @Test
    void configurado_comChaveEmBranco_deveRetornarFalse() {
        GeminiClient geminiClient = new GeminiClient("   ", criseDetectorService);

        assertThat(geminiClient.configurado()).isFalse();
    }

    @Test
    void configurado_comChave_deveRetornarTrue() {
        GeminiClient geminiClient = new GeminiClient("chave-fake", criseDetectorService);

        assertThat(geminiClient.configurado()).isTrue();
    }

    @Test
    void gerarResposta_semChaveEUltimaMensagemNormal_deveRetornarFallbackGenerico() {
        GeminiClient geminiClient = new GeminiClient("", criseDetectorService);
        List<ChatMessage> historico = List.of(new ChatMessage("user", "Tive uma semana difícil no trabalho."));

        String resposta = geminiClient.gerarResposta(historico);

        assertThat(resposta).contains("instabilidade técnica");
    }

    @Test
    void gerarResposta_semChaveEUltimaMensagemDeCrise_deveRetornarFallbackDeCriseComTecnicaDeRespiracao() {
        GeminiClient geminiClient = new GeminiClient("", criseDetectorService);
        List<ChatMessage> historico = List.of(new ChatMessage("user", "Estou tendo uma crise de pânico agora."));

        String resposta = geminiClient.gerarResposta(historico);

        assertThat(resposta).contains("4-7-8");
    }

    @Test
    void gerarResposta_semMensagemDeUsuarioNoHistorico_deveRetornarFallbackGenericoSemLancar() {
        GeminiClient geminiClient = new GeminiClient("", criseDetectorService);

        String resposta = geminiClient.gerarResposta(List.of());

        assertThat(resposta).contains("instabilidade técnica");
    }

    @Test
    void gerarResposta_fallbackDeCrise_deveSerClassificadoComoCrisePeloDetector() {
        // Regressão: o fallback local precisa conter as frases que o próprio CriseDetectorService
        // reconhece, senão o pipeline de plantão do ChatbotService nunca é acionado sem uma chave
        // real do Gemini configurada.
        GeminiClient geminiClient = new GeminiClient("", criseDetectorService);
        List<ChatMessage> historico = List.of(new ChatMessage("user", "Penso em suicídio, socorro"));

        String resposta = geminiClient.gerarResposta(historico);

        assertThat(criseDetectorService.classificar(resposta)).isEqualTo(StatusTriagem.CRISE);
    }
}
