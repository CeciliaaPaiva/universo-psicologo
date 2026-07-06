package br.com.unipsi.agenda.controller;

import br.com.unipsi.agenda.service.GoogleOAuthService;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda/google")
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

    @Value("${unipsi.frontend-origin}")
    private String frontendOrigin;

    @GetMapping("/auth-url")
    @PreAuthorize("hasRole('PSICOLOGO')")
    public Map<String, String> urlAutorizacao(Authentication auth) {
        UUID psicologoId = UUID.fromString(auth.getName());
        return Map.of("url", googleOAuthService.gerarUrlAutorizacao(psicologoId));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam String state) {
        googleOAuthService.tratarCallback(code, state);
        return ResponseEntity.status(302)
                .location(URI.create(frontendOrigin + "/agenda?google=conectado"))
                .build();
    }
}
