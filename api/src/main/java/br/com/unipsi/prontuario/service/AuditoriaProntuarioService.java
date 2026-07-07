package br.com.unipsi.prontuario.service;

import br.com.unipsi.prontuario.domain.AcaoAuditoria;
import br.com.unipsi.prontuario.domain.AuditoriaProntuario;
import br.com.unipsi.prontuario.domain.Prontuario;
import br.com.unipsi.prontuario.repository.AuditoriaProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaProntuarioService {

    private final AuditoriaProntuarioRepository auditoriaProntuarioRepository;

    @Transactional
    public void registrar(Prontuario prontuario, AcaoAuditoria acao) {
        auditoriaProntuarioRepository.save(AuditoriaProntuario.builder()
                .prontuario(prontuario)
                .psicologo(prontuario.getPsicologo())
                .acao(acao)
                .build());
    }
}
