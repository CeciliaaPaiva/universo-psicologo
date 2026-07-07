package br.com.unipsi.marketplace.controller;

import br.com.unipsi.marketplace.dto.PsicologoPerfilResponse;
import br.com.unipsi.marketplace.dto.PsicologoResumoResponse;
import br.com.unipsi.marketplace.service.MarketplaceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace/psicologos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PACIENTE')")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @GetMapping
    public List<PsicologoResumoResponse> buscar(
            Authentication auth, @RequestParam(required = false) String especialidade) {
        return marketplaceService.buscar(pacienteId(auth), especialidade);
    }

    @GetMapping("/{id}")
    public PsicologoPerfilResponse perfil(Authentication auth, @PathVariable UUID id) {
        return marketplaceService.perfil(pacienteId(auth), id);
    }

    private UUID pacienteId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
