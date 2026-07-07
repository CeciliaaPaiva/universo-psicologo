package br.com.unipsi.chatbot.service;

import br.com.unipsi.chatbot.domain.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Histórico de conversa do chatbot no Redis, TTL de 30 min. Chave prefixada com "unipsi:" —
 * o usuário Redis do ambiente de dev tem ACL restrita a esse prefixo.
 */
@Service
@RequiredArgsConstructor
public class ConversacaoStateService {

    private static final Logger log = LoggerFactory.getLogger(ConversacaoStateService.class);
    private static final String PREFIXO = "unipsi:chatbot:conversa:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<ChatMessage> obterHistorico(String sessionId) {
        String json = redisTemplate.opsForValue().get(PREFIXO + sessionId);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("Falha ao desserializar histórico da sessão {} — iniciando nova conversa", sessionId);
            return new ArrayList<>();
        }
    }

    public void salvarHistorico(String sessionId, List<ChatMessage> historico) {
        try {
            String json = objectMapper.writeValueAsString(historico);
            redisTemplate.opsForValue().set(PREFIXO + sessionId, json, TTL);
        } catch (JsonProcessingException e) {
            log.warn("Falha ao serializar histórico da sessão {}: {}", sessionId, e.getMessage());
        }
    }
}
