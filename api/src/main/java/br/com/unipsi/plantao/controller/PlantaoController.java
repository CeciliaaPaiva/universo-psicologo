package br.com.unipsi.plantao.controller;

import br.com.unipsi.plantao.dto.AtivarDisponibilidadeRequest;
import br.com.unipsi.plantao.dto.CriarDisponibilidadeRequest;
import br.com.unipsi.plantao.dto.DisponibilidadeResponse;
import br.com.unipsi.plantao.dto.StatusPlantaoResponse;
import br.com.unipsi.plantao.service.PlantaoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plantao")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PSICOLOGO')")
public class PlantaoController {

    private final PlantaoService plantaoService;

    @PostMapping("/disponibilidade")
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadeResponse criar(Authentication auth, @RequestBody @Valid CriarDisponibilidadeRequest pedido) {
        return plantaoService.criar(psicologoId(auth), pedido);
    }

    @GetMapping("/status")
    public StatusPlantaoResponse status(Authentication auth) {
        return plantaoService.status(psicologoId(auth));
    }

    @PatchMapping("/{id}/ativar")
    public void ativar(Authentication auth, @PathVariable UUID id, @RequestBody @Valid AtivarDisponibilidadeRequest pedido) {
        plantaoService.ativar(psicologoId(auth), id, pedido.ativo());
    }

    private UUID psicologoId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
