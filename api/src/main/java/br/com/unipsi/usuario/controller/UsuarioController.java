package br.com.unipsi.usuario.controller;

import br.com.unipsi.usuario.dto.AtualizarPerfilPsicologoRequest;
import br.com.unipsi.usuario.dto.PerfilPsicologoResponse;
import br.com.unipsi.usuario.service.PsicologoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios/psicologo")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PSICOLOGO')")
public class UsuarioController {

    private final PsicologoService psicologoService;

    @GetMapping("/perfil")
    public PerfilPsicologoResponse perfil(Authentication auth) {
        return psicologoService.buscarPerfil(psicologoId(auth));
    }

    @PutMapping(value = "/perfil", consumes = "multipart/form-data")
    public PerfilPsicologoResponse atualizarPerfil(
            Authentication auth,
            @RequestPart("dados") @Valid AtualizarPerfilPsicologoRequest dados,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        return psicologoService.atualizarPerfil(psicologoId(auth), dados, foto);
    }

    private UUID psicologoId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
