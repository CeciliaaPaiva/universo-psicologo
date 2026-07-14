package br.com.unipsi.notificacao.controller;

import br.com.unipsi.notificacao.dto.ContadorNaoLidasResponse;
import br.com.unipsi.notificacao.dto.NotificacaoResponse;
import br.com.unipsi.notificacao.service.NotificacaoService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    public List<NotificacaoResponse> listar(Authentication auth) {
        return notificacaoService.listar(usuarioId(auth));
    }

    @GetMapping("/nao-lidas")
    public ContadorNaoLidasResponse contarNaoLidas(Authentication auth) {
        return new ContadorNaoLidasResponse(notificacaoService.contarNaoLidas(usuarioId(auth)));
    }

    @PostMapping("/{id}/lida")
    public ResponseEntity<Void> marcarLida(Authentication auth, @PathVariable UUID id) {
        notificacaoService.marcarLida(usuarioId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lidas")
    public ResponseEntity<Void> marcarTodasLidas(Authentication auth) {
        notificacaoService.marcarTodasLidas(usuarioId(auth));
        return ResponseEntity.noContent().build();
    }

    private UUID usuarioId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
