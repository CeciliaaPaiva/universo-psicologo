package br.com.unipsi.auth.controller;

import br.com.unipsi.auth.dto.AuthResponse;
import br.com.unipsi.auth.dto.LoginRequest;
import br.com.unipsi.auth.dto.RefreshRequest;
import br.com.unipsi.auth.dto.RegisterPacienteRequest;
import br.com.unipsi.auth.dto.RegisterPsicologoRequest;
import br.com.unipsi.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register/psicologo", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarPsicologo(
            @RequestPart("dados") @Valid RegisterPsicologoRequest dados,
            @RequestPart("curriculo") MultipartFile curriculo) {
        authService.registrarPsicologo(dados, curriculo);
    }

    @PostMapping("/register/paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarPaciente(@RequestBody @Valid RegisterPacienteRequest dados) {
        authService.registrarPaciente(dados);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest dados) {
        return authService.login(dados);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest dados) {
        return authService.refresh(dados.refreshToken());
    }
}
