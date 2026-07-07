package br.com.unipsi.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ChatRateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ChatRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new ChatRateLimitService(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void excedeuLimite_abaixoDoLimite_deveRetornarFalse() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.2.3.4")).thenReturn(5L);

        assertThat(rateLimitService.excedeuLimite("1.2.3.4")).isFalse();
    }

    @Test
    void excedeuLimite_exatamenteNoLimite_deveRetornarFalse() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.2.3.4")).thenReturn(20L);

        assertThat(rateLimitService.excedeuLimite("1.2.3.4")).isFalse();
    }

    @Test
    void excedeuLimite_acimaDoLimite_deveRetornarTrue() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.2.3.4")).thenReturn(21L);

        assertThat(rateLimitService.excedeuLimite("1.2.3.4")).isTrue();
    }

    @Test
    void primeiraChamada_deveDefinirExpiracaoDeUmMinuto() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.2.3.4")).thenReturn(1L);

        rateLimitService.excedeuLimite("1.2.3.4");

        verify(redisTemplate).expire("unipsi:chatbot:ratelimit:1.2.3.4", Duration.ofMinutes(1));
    }

    @Test
    void chamadasSubsequentes_naoDevemRedefinirExpiracao() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.2.3.4")).thenReturn(2L);

        rateLimitService.excedeuLimite("1.2.3.4");

        verify(redisTemplate, org.mockito.Mockito.never()).expire(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ipsDistintos_naoCompartilhamLimite() {
        when(valueOperations.increment("unipsi:chatbot:ratelimit:1.1.1.1")).thenReturn(21L);
        when(valueOperations.increment("unipsi:chatbot:ratelimit:2.2.2.2")).thenReturn(1L);

        assertThat(rateLimitService.excedeuLimite("1.1.1.1")).isTrue();
        assertThat(rateLimitService.excedeuLimite("2.2.2.2")).isFalse();
    }
}
