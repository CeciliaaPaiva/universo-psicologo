package br.com.unipsi.admin.controller;

import br.com.unipsi.admin.dto.DecisaoAprovacaoRequest;
import br.com.unipsi.admin.dto.PsicologoPendenteResponse;
import br.com.unipsi.admin.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/aprovacoes")
    public List<PsicologoPendenteResponse> listarPendentes() {
        return adminService.listarPendentes();
    }

    @PutMapping("/aprovacoes/{id}")
    public ResponseEntity<Void> decidir(@PathVariable UUID id, @RequestBody @Valid DecisaoAprovacaoRequest decisao) {
        adminService.decidir(id, decisao);
        return ResponseEntity.noContent().build();
    }
}
