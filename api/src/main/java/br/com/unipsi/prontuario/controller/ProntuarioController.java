package br.com.unipsi.prontuario.controller;

import br.com.unipsi.prontuario.dto.AnotacaoResponse;
import br.com.unipsi.prontuario.dto.AtualizarCodinomeRequest;
import br.com.unipsi.prontuario.dto.CriarAnotacaoRequest;
import br.com.unipsi.prontuario.dto.CriarProntuarioRequest;
import br.com.unipsi.prontuario.dto.ProntuarioResponse;
import br.com.unipsi.prontuario.service.ProntuarioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prontuario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PSICOLOGO')")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    @PostMapping("/pacientes")
    @ResponseStatus(HttpStatus.CREATED)
    public ProntuarioResponse criar(Authentication auth, @RequestBody @Valid CriarProntuarioRequest pedido) {
        return prontuarioService.criarProntuario(psicologoId(auth), pedido);
    }

    @GetMapping("/pacientes")
    public List<ProntuarioResponse> listar(Authentication auth) {
        return prontuarioService.listar(psicologoId(auth));
    }

    @PutMapping("/{codinome}")
    public ProntuarioResponse atualizarCodinome(
            Authentication auth, @PathVariable String codinome, @RequestBody @Valid AtualizarCodinomeRequest pedido) {
        return prontuarioService.atualizarCodinome(psicologoId(auth), codinome, pedido.novoCodinome());
    }

    @PostMapping("/{codinome}/anotacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AnotacaoResponse criarAnotacao(
            Authentication auth, @PathVariable String codinome, @RequestBody @Valid CriarAnotacaoRequest pedido) {
        return prontuarioService.criarAnotacao(psicologoId(auth), codinome, pedido);
    }

    @GetMapping("/{codinome}/anotacoes")
    public List<AnotacaoResponse> listarAnotacoes(
            Authentication auth, @PathVariable String codinome, @RequestParam(required = false) String busca) {
        return prontuarioService.listarAnotacoes(psicologoId(auth), codinome, busca);
    }

    private UUID psicologoId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
