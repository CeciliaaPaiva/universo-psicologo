package br.com.unipsi.prontuario.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import br.com.unipsi.prontuario.domain.AcaoAuditoria;
import br.com.unipsi.prontuario.domain.AuditoriaProntuario;
import br.com.unipsi.prontuario.domain.Prontuario;
import br.com.unipsi.prontuario.repository.AuditoriaProntuarioRepository;
import br.com.unipsi.usuario.domain.Psicologo;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditoriaProntuarioServiceTest {

    @Mock
    private AuditoriaProntuarioRepository auditoriaProntuarioRepository;

    @InjectMocks
    private AuditoriaProntuarioService auditoriaProntuarioService;

    @Test
    void registrar_leitura_devePersistirComAcaoCorreta() {
        Psicologo psicologo = Psicologo.builder().id(UUID.randomUUID()).build();
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).build();

        auditoriaProntuarioService.registrar(prontuario, AcaoAuditoria.LEITURA);

        verify(auditoriaProntuarioRepository).save(any(AuditoriaProntuario.class));
    }

    @Test
    void registrar_escrita_devePersistirComAcaoCorreta() {
        Psicologo psicologo = Psicologo.builder().id(UUID.randomUUID()).build();
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).build();

        auditoriaProntuarioService.registrar(prontuario, AcaoAuditoria.ESCRITA);

        verify(auditoriaProntuarioRepository).save(any(AuditoriaProntuario.class));
    }
}
