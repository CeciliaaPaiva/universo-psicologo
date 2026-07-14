package br.com.unipsi.financeiro.controller;

import br.com.unipsi.financeiro.dto.CobrancaResponse;
import br.com.unipsi.financeiro.dto.RelatorioFinanceiroResponse;
import br.com.unipsi.financeiro.service.CobrancaService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final CobrancaService cobrancaService;

    @GetMapping("/cobrancas")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<CobrancaResponse> listarCobrancas(Authentication auth) {
        return cobrancaService.listarPorPaciente(usuarioId(auth));
    }

    @PostMapping("/cobrancas/{id}/pagar")
    @PreAuthorize("hasRole('PACIENTE')")
    public CobrancaResponse pagar(Authentication auth, @PathVariable UUID id) {
        return cobrancaService.pagar(usuarioId(auth), id);
    }

    @PostMapping("/cobrancas/{id}/cancelar")
    @PreAuthorize("hasRole('PSICOLOGO')")
    public CobrancaResponse cancelar(Authentication auth, @PathVariable UUID id) {
        return cobrancaService.cancelar(usuarioId(auth), id);
    }

    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('PSICOLOGO')")
    public RelatorioFinanceiroResponse relatorio(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return cobrancaService.relatorio(usuarioId(auth), inicio, fim);
    }

    private UUID usuarioId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
