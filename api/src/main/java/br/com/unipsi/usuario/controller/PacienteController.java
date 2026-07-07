package br.com.unipsi.usuario.controller;

import br.com.unipsi.usuario.dto.AtualizarPerfilPacienteRequest;
import br.com.unipsi.usuario.dto.PerfilPacienteResponse;
import br.com.unipsi.usuario.service.PacienteService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/paciente")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PACIENTE')")
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping("/perfil")
    public PerfilPacienteResponse perfil(Authentication auth) {
        return pacienteService.buscarPerfil(pacienteId(auth));
    }

    @PutMapping("/perfil")
    public PerfilPacienteResponse atualizarPerfil(
            Authentication auth, @RequestBody @Valid AtualizarPerfilPacienteRequest dados) {
        return pacienteService.atualizarPerfil(pacienteId(auth), dados);
    }

    private UUID pacienteId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
