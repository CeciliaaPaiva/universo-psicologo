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

    /**
     * Token de curta duração usado como "state" em fluxos de redirecionamento externos
     * (ex.: OAuth do Google Calendar), onde o Authorization header não está disponível.
     */
    public String gerarTokenEstado(UUID usuarioId, String finalidade) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("finalidade", finalidade)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(10, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    public UUID validarTokenEstado(String token, String finalidade) {
        Claims claims = validarEExtrairClaims(token);
        if (!finalidade.equals(claims.get("finalidade", String.class))) {
            throw new io.jsonwebtoken.JwtException("Token de estado inválido");
        }
        return UUID.fromString(claims.getSubject());
    }
}
