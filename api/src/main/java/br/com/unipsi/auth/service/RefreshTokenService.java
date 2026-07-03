package br.com.unipsi.auth.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIXO = "unipsi:refresh-token:";

    private final StringRedisTemplate redisTemplate;

    @Value("${unipsi.jwt.refresh-token-ttl-dias}")
    private long refreshTokenTtlDias;

    public String gerar(UUID usuarioId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIXO + token, usuarioId.toString(), Duration.ofDays(refreshTokenTtlDias));
        return token;
    }

    public Optional<UUID> validar(String token) {
        String usuarioId = redisTemplate.opsForValue().get(PREFIXO + token);
        return Optional.ofNullable(usuarioId).map(UUID::fromString);
    }

    public void revogar(String token) {
        redisTemplate.delete(PREFIXO + token);
    }
}
