package br.com.unipsi.agenda.controller;

import br.com.unipsi.agenda.dto.AgendarSessaoRequest;
import br.com.unipsi.agenda.dto.SessaoResponse;
import br.com.unipsi.agenda.service.SessaoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda/sessoes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PACIENTE')")
public class SessaoController {

    private final SessaoService sessaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse agendar(Authentication auth, @RequestBody @Valid AgendarSessaoRequest pedido) {
        return sessaoService.agendar(pacienteId(auth), pedido);
    }

    @GetMapping
    public List<SessaoResponse> listar(Authentication auth) {
        return sessaoService.listar(pacienteId(auth));
    }

    private UUID pacienteId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
