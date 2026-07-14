package br.com.unipsi.financeiro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.domain.TipoAtendimento;
import br.com.unipsi.financeiro.domain.Cobranca;
import br.com.unipsi.financeiro.domain.StatusCobranca;
import br.com.unipsi.financeiro.dto.CobrancaResponse;
import br.com.unipsi.financeiro.repository.CobrancaRepository;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CobrancaServiceTest {

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private CobrancaService cobrancaService;

    private UUID pacienteId;
    private UUID psicologoId;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        psicologoId = UUID.randomUUID();

        Usuario usuarioPaciente = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        Paciente paciente =
                Paciente.builder().id(pacienteId).usuario(usuarioPaciente).faixaRenda(FaixaRenda.FAIXA_1).build();

        Usuario usuarioPsicologo = Usuario.builder().nome("Psicólogo Teste").email("psi@teste.com").build();
        Psicologo psicologo = Psicologo.builder().id(psicologoId).usuario(usuarioPsicologo).build();

        Slot slot = Slot.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologo)
                .inicio(LocalDateTime.now().minusHours(1))
                .fim(LocalDateTime.now())
                .disponivel(false)
                .build();

        sessao = Sessao.builder()
                .id(UUID.randomUUID())
                .slot(slot)
                .paciente(paciente)
                .psicologo(psicologo)
                .modalidade(Modalidade.AVULSA)
                .tipoAtendimento(TipoAtendimento.INDIVIDUAL)
                .valorSessao(new BigDecimal("60.00"))
                .taxaPlataforma(new BigDecimal("12.00"))
                .valorLiquido(new BigDecimal("48.00"))
                .status(StatusSessao.REALIZADA)
                .build();
    }

    @Test
    void gerar_deveCriarCobrancaPendenteComValoresDaSessaoENotificarPaciente() {
        when(cobrancaRepository.save(any(Cobranca.class))).thenAnswer(inv -> {
            Cobranca c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Cobranca cobranca = cobrancaService.gerar(sessao);

        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
        assertThat(cobranca.getValorBruto()).isEqualByComparingTo("60.00");
        assertThat(cobranca.getValorLiquido()).isEqualByComparingTo("48.00");
        org.mockito.Mockito.verify(emailService)
                .enviarCobrancaGerada("paciente@teste.com", "Paciente Teste", new BigDecimal("60.00"));
    }

    @Test
    void pagar_cobrancaPendenteDoPaciente_deveMarcarPagaENotificarPsicologo() {
        Cobranca cobranca = Cobranca.builder()
                .id(UUID.randomUUID())
                .sessao(sessao)
                .valorBruto(new BigDecimal("60.00"))
                .taxaPlataforma(new BigDecimal("12.00"))
                .valorLiquido(new BigDecimal("48.00"))
                .status(StatusCobranca.PENDENTE)
                .build();
        when(cobrancaRepository.findById(cobranca.getId())).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenAnswer(inv -> inv.getArgument(0));

        CobrancaResponse resposta = cobrancaService.pagar(pacienteId, cobranca.getId());

        assertThat(resposta.status()).isEqualTo(StatusCobranca.PAGO);
        org.mockito.Mockito.verify(emailService)
                .enviarCobrancaPaga("psi@teste.com", "Psicólogo Teste", new BigDecimal("48.00"));
    }

    @Test
    void pagar_deOutroPaciente_deveLancarExcecao() {
        Cobranca cobranca = Cobranca.builder()
                .id(UUID.randomUUID())
                .sessao(sessao)
                .status(StatusCobranca.PENDENTE)
                .build();
        when(cobrancaRepository.findById(cobranca.getId())).thenReturn(Optional.of(cobranca));

        assertThatThrownBy(() -> cobrancaService.pagar(UUID.randomUUID(), cobranca.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pagar_cobrancaJaPaga_deveLancarExcecao() {
        Cobranca cobranca = Cobranca.builder()
                .id(UUID.randomUUID())
                .sessao(sessao)
                .status(StatusCobranca.PAGO)
                .build();
        when(cobrancaRepository.findById(cobranca.getId())).thenReturn(Optional.of(cobranca));

        assertThatThrownBy(() -> cobrancaService.pagar(pacienteId, cobranca.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelar_cobrancaPendenteDoPsicologo_deveMarcarCanceladaENotificarPaciente() {
        Cobranca cobranca = Cobranca.builder()
                .id(UUID.randomUUID())
                .sessao(sessao)
                .status(StatusCobranca.PENDENTE)
                .build();
        when(cobrancaRepository.findById(cobranca.getId())).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenAnswer(inv -> inv.getArgument(0));

        CobrancaResponse resposta = cobrancaService.cancelar(psicologoId, cobranca.getId());

        assertThat(resposta.status()).isEqualTo(StatusCobranca.CANCELADO);
        org.mockito.Mockito.verify(emailService).enviarCobrancaCancelada("paciente@teste.com", "Paciente Teste");
    }
}
