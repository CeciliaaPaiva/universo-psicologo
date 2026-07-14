package br.com.unipsi.mensagem.controller;

import br.com.unipsi.mensagem.dto.ContatoMensagemResponse;
import br.com.unipsi.mensagem.dto.EnviarMensagemRequest;
import br.com.unipsi.mensagem.dto.MensagemResponse;
import br.com.unipsi.mensagem.service.MensagemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mensagens")
@RequiredArgsConstructor
public class MensagemController {

    private final MensagemService mensagemService;

    @GetMapping("/contatos")
    public List<ContatoMensagemResponse> contatos(Authentication auth) {
        UUID usuarioId = usuarioId(auth);
        return ehPsicologo(auth)
                ? mensagemService.listarContatosDoPsicologo(usuarioId)
                : mensagemService.listarContatosDoPaciente(usuarioId);
    }

    @GetMapping("/{outroId}")
    public List<MensagemResponse> conversa(Authentication auth, @PathVariable UUID outroId) {
        return mensagemService.listarConversa(usuarioId(auth), ehPsicologo(auth), outroId);
    }

    @PostMapping("/{outroId}")
    public MensagemResponse enviar(
            Authentication auth, @PathVariable UUID outroId, @RequestBody @Valid EnviarMensagemRequest pedido) {
        return mensagemService.enviar(usuarioId(auth), ehPsicologo(auth), outroId, pedido.conteudo());
    }

    private UUID usuarioId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }

    private boolean ehPsicologo(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_PSICOLOGO"::equals);
    }
}
