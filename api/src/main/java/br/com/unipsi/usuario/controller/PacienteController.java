package br.com.unipsi.usuario.controller;

import br.com.unipsi.usuario.dto.AnamneseRequest;
import br.com.unipsi.usuario.dto.AnamneseResponse;
import br.com.unipsi.usuario.dto.AtualizarPerfilPacienteRequest;
import br.com.unipsi.usuario.dto.PerfilPacienteResponse;
import br.com.unipsi.usuario.service.AnamneseService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios/paciente")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PACIENTE')")
public class PacienteController {

    private final PacienteService pacienteService;
    private final AnamneseService anamneseService;

    @GetMapping("/perfil")
    public PerfilPacienteResponse perfil(Authentication auth) {
        return pacienteService.buscarPerfil(pacienteId(auth));
    }

    @PutMapping(value = "/perfil", consumes = "multipart/form-data")
    public PerfilPacienteResponse atualizarPerfil(
            Authentication auth,
            @RequestPart("dados") @Valid AtualizarPerfilPacienteRequest dados,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        return pacienteService.atualizarPerfil(pacienteId(auth), dados, foto);
    }

    @GetMapping("/anamnese")
    public AnamneseResponse anamnese(Authentication auth) {
        return anamneseService.buscar(pacienteId(auth));
    }

    @PutMapping("/anamnese")
    public AnamneseResponse atualizarAnamnese(Authentication auth, @RequestBody @Valid AnamneseRequest dados) {
        return anamneseService.salvar(pacienteId(auth), dados);
    }

    private UUID pacienteId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
