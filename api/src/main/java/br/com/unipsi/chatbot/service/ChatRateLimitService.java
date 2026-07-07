package br.com.unipsi.chatbot.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Limita mensagens do chatbot a 20 por IP a cada janela de 1 minuto, usando um contador no Redis
 * com expiração definida apenas na primeira chamada da janela (fixed window).
 */
@Service
@RequiredArgsConstructor
public class ChatRateLimitService {

    private static final String PREFIXO = "unipsi:chatbot:ratelimit:";
    private static final int LIMITE_POR_MINUTO = 20;

    private final StringRedisTemplate redisTemplate;

    public boolean excedeuLimite(String ip) {
        String chave = PREFIXO + ip;
        Long contagem = redisTemplate.opsForValue().increment(chave);
        if (contagem != null && contagem == 1L) {
            redisTemplate.expire(chave, Duration.ofMinutes(1));
        }
        return contagem != null && contagem > LIMITE_POR_MINUTO;
    }
}
