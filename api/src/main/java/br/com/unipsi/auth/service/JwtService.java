package br.com.unipsi.auth.service;

import br.com.unipsi.usuario.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long accessTokenTtlMinutes;

    public JwtService(
            @Value("${unipsi.jwt.secret}") String secret,
            @Value("${unipsi.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("role", usuario.getRole().name())
                .claim("nome", usuario.getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public Claims validarEExtrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extrairUsuarioId(String token) {
        return UUID.fromString(validarEExtrairClaims(token).getSubject());
    }
}
