package br.com.unipsi.mensagem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.financeiro.domain.StatusCobranca;
import br.com.unipsi.financeiro.repository.CobrancaRepository;
import br.com.unipsi.mensagem.domain.CanalMensagemFechadoException;
import br.com.unipsi.mensagem.domain.Mensagem;
import br.com.unipsi.mensagem.dto.MensagemResponse;
import br.com.unipsi.mensagem.repository.MensagemRepository;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import br.com.unipsi.usuario.repository.UsuarioRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock
    private MensagemRepository mensagemRepository;

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private MensagemService mensagemService;

    private UUID pacienteId;
    private UUID psicologoId;
    private Paciente paciente;
    private Psicologo psicologo;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        psicologoId = UUID.randomUUID();
        Usuario usuarioPaciente = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        paciente = Paciente.builder().id(pacienteId).usuario(usuarioPaciente).faixaRenda(FaixaRenda.FAIXA_1).build();
        Usuario usuarioPsicologo = Usuario.builder().nome("Psi Teste").email("psi@teste.com").build();
        psicologo = Psicologo.builder().id(psicologoId).usuario(usuarioPsicologo).build();
    }

    @Test
    void enviar_semCobrancaPaga_deveLancarCanalFechado() {
        when(cobrancaRepository.existsPagaEntrePacienteEPsicologo(pacienteId, psicologoId, StatusCobranca.PAGO))
                .thenReturn(false);

        assertThatThrownBy(() -> mensagemService.enviar(pacienteId, false, psicologoId, "Oi"))
                .isInstanceOf(CanalMensagemFechadoException.class);
    }

    @Test
    void enviar_pacienteParaPsicologoComCobrancaPaga_devePersistirENotificarPsicologo() {
        when(cobrancaRepository.existsPagaEntrePacienteEPsicologo(pacienteId, psicologoId, StatusCobranca.PAGO))
                .thenReturn(true);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(invocation -> {
            Mensagem m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        MensagemResponse resposta = mensagemService.enviar(pacienteId, false, psicologoId, "Oi, tudo bem?");

        assertThat(resposta.conteudo()).isEqualTo("Oi, tudo bem?");
        assertThat(resposta.remetenteId()).isEqualTo(pacienteId);
        org.mockito.Mockito.verify(notificacaoService).criar(
                org.mockito.ArgumentMatchers.eq(psicologoId), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void enviar_psicologoParaPacienteComCobrancaPaga_deveNotificarPaciente() {
        when(cobrancaRepository.existsPagaEntrePacienteEPsicologo(pacienteId, psicologoId, StatusCobranca.PAGO))
                .thenReturn(true);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(invocation -> {
            Mensagem m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        mensagemService.enviar(psicologoId, true, pacienteId, "Como você está?");

        org.mockito.Mockito.verify(notificacaoService).criar(
                org.mockito.ArgumentMatchers.eq(pacienteId), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void listarConversa_semCobrancaPaga_deveLancarCanalFechado() {
        when(cobrancaRepository.existsPagaEntrePacienteEPsicologo(pacienteId, psicologoId, StatusCobranca.PAGO))
                .thenReturn(false);

        assertThatThrownBy(() -> mensagemService.listarConversa(pacienteId, false, psicologoId))
                .isInstanceOf(CanalMensagemFechadoException.class);
    }

    @Test
    void listarConversa_comCobrancaPaga_deveMarcarMensagensDoOutroComoLidas() {
        when(cobrancaRepository.existsPagaEntrePacienteEPsicologo(pacienteId, psicologoId, StatusCobranca.PAGO))
                .thenReturn(true);
        Mensagem naoLida = Mensagem.builder()
                .id(UUID.randomUUID())
                .paciente(paciente)
                .psicologo(psicologo)
                .remetenteId(psicologoId)
                .conteudo("Olá")
                .lida(false)
                .build();
        when(mensagemRepository.findByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
                        pacienteId, psicologoId, pacienteId))
                .thenReturn(List.of(naoLida));
        when(mensagemRepository.findByPacienteIdAndPsicologoIdOrderByCriadaEmAsc(pacienteId, psicologoId))
                .thenReturn(List.of(naoLida));

        List<MensagemResponse> respostas = mensagemService.listarConversa(pacienteId, false, psicologoId);

        assertThat(respostas).hasSize(1);
        assertThat(naoLida.isLida()).isTrue();
    }

    @Test
    void listarContatosDoPaciente_deveRetornarPsicologosComCobrancaPaga() {
        when(cobrancaRepository.buscarPsicologosComCobrancaPaga(pacienteId, StatusCobranca.PAGO))
                .thenReturn(List.of(psicologoId));
        when(usuarioRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo.getUsuario()));
        when(mensagemRepository.countByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
                        pacienteId, psicologoId, pacienteId))
                .thenReturn(2L);

        var contatos = mensagemService.listarContatosDoPaciente(pacienteId);

        assertThat(contatos).hasSize(1);
        assertThat(contatos.get(0).nome()).isEqualTo("Psi Teste");
        assertThat(contatos.get(0).naoLidas()).isEqualTo(2L);
    }
}
