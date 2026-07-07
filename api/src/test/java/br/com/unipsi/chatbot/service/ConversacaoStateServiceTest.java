package br.com.unipsi.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.unipsi.chatbot.domain.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ConversacaoStateServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ConversacaoStateService conversacaoStateService;

    @BeforeEach
    void setUp() {
        conversacaoStateService = new ConversacaoStateService(redisTemplate, new ObjectMapper());
    }

    @Test
    void obterHistorico_semHistoricoPrevio_deveRetornarListaVazia() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("unipsi:chatbot:conversa:sessao-1")).thenReturn(null);

        List<ChatMessage> historico = conversacaoStateService.obterHistorico("sessao-1");

        assertThat(historico).isEmpty();
    }

    @Test
    void obterHistorico_comHistoricoPrevio_deveDesserializar() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("unipsi:chatbot:conversa:sessao-1"))
                .thenReturn("[{\"role\":\"user\",\"conteudo\":\"oi\"}]");

        List<ChatMessage> historico = conversacaoStateService.obterHistorico("sessao-1");

        assertThat(historico).containsExactly(new ChatMessage("user", "oi"));
    }

    @Test
    void obterHistorico_jsonInvalido_deveRetornarListaVaziaSemLancar() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("unipsi:chatbot:conversa:sessao-1")).thenReturn("não é json");

        List<ChatMessage> historico = conversacaoStateService.obterHistorico("sessao-1");

        assertThat(historico).isEmpty();
    }

    @Test
    void salvarHistorico_deveSerializarESalvarComTtlDe30Minutos() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<ChatMessage> historico = List.of(new ChatMessage("user", "oi"), new ChatMessage("model", "olá!"));

        conversacaoStateService.salvarHistorico("sessao-1", historico);

        verify(valueOperations)
                .set(
                        eq("unipsi:chatbot:conversa:sessao-1"),
                        anyString(),
                        eq(Duration.ofMinutes(30)));
    }
}
