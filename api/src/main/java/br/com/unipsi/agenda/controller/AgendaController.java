package br.com.unipsi.agenda.controller;

import br.com.unipsi.agenda.dto.CriarSlotRequest;
import br.com.unipsi.agenda.dto.SlotResponse;
import br.com.unipsi.agenda.service.AgendaService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda/slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PSICOLOGO')")
public class AgendaController {

    private final AgendaService agendaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<SlotResponse> criar(Authentication auth, @RequestBody @Valid List<CriarSlotRequest> pedidos) {
        return agendaService.criarSlots(psicologoId(auth), pedidos);
    }

    @GetMapping
    public List<SlotResponse> listar(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return agendaService.listar(psicologoId(auth), inicio, fim);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            Authentication auth, @PathVariable UUID id, @RequestParam(required = false) String motivo) {
        agendaService.cancelar(psicologoId(auth), id, motivo);
        return ResponseEntity.noContent().build();
    }

    private UUID psicologoId(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
