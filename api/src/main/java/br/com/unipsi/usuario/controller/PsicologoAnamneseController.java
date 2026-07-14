package br.com.unipsi.usuario.controller;

import br.com.unipsi.usuario.dto.AnamnesePsicologoResponse;
import br.com.unipsi.usuario.service.AnamneseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/psicologo/pacientes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PSICOLOGO')")
public class PsicologoAnamneseController {

    private final AnamneseService anamneseService;

    @GetMapping("/{pacienteId}/anamnese")
    public AnamnesePsicologoResponse anamnese(Authentication auth, @PathVariable UUID pacienteId) {
        return anamneseService.buscarParaPsicologo(psicologoId(auth), pacienteId);
    }

    private UUID psicologoId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
