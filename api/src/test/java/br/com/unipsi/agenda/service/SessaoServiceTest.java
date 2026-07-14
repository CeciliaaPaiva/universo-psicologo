package br.com.unipsi.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.domain.TipoAtendimento;
import br.com.unipsi.agenda.dto.AgendarSessaoRequest;
import br.com.unipsi.agenda.dto.SessaoResponse;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.financeiro.service.CobrancaService;
import br.com.unipsi.marketplace.service.PrecificacaoService;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class SessaoServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PrecificacaoService precificacaoService;

    @Mock
    private EmailService emailService;

    @Mock
    private CobrancaService cobrancaService;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private SessaoService sessaoService;

    private UUID pacienteId;
    private Paciente paciente;
    private Psicologo psicologo;
    private UUID slotId;
    private Slot slot;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        Usuario usuarioPaciente = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        paciente = Paciente.builder().id(pacienteId).usuario(usuarioPaciente).faixaRenda(FaixaRenda.FAIXA_1).build();

        Usuario usuarioPsicologo = Usuario.builder().nome("Psicólogo Teste").email("psi@teste.com").build();
        psicologo = Psicologo.builder().id(UUID.randomUUID()).usuario(usuarioPsicologo).build();

        slotId = UUID.randomUUID();
        slot = Slot.builder()
                .id(slotId)
                .psicologo(psicologo)
                .inicio(LocalDateTime.now().plusDays(1))
                .fim(LocalDateTime.now().plusDays(1).plusHours(1))
                .disponivel(true)
                .build();
    }

    @Test
    void agendar_slotDisponivel_deveCriarSessaoEMarcarSlotIndisponivel() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(precificacaoService.calcularValorSessao(FaixaRenda.FAIXA_1, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("60.00"));
        when(precificacaoService.calcularTaxa(new BigDecimal("60.00"))).thenReturn(new BigDecimal("12.00"));
        when(precificacaoService.calcularValorLiquido(new BigDecimal("60.00"), new BigDecimal("12.00")))
                .thenReturn(new BigDecimal("48.00"));
        when(precificacaoService.calcularValorPacoteTotal(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("228.00"));
        when(precificacaoService.calcularEconomiaPacote(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("12.00"));
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> {
            Sessao sessao = invocation.getArgument(0);
            sessao.setId(UUID.randomUUID());
            return sessao;
        });

        SessaoResponse resposta = sessaoService.agendar(
                pacienteId, new AgendarSessaoRequest(slotId, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL));

        assertThat(slot.isDisponivel()).isFalse();
        assertThat(resposta.valorSessao()).isEqualByComparingTo("60.00");
        assertThat(resposta.status()).isEqualTo(StatusSessao.AGENDADA);
    }

    @Test
    void agendar_slotOcupado_deveLancarSlotIndisponivelException() {
        slot.setDisponivel(false);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> sessaoService.agendar(
                        pacienteId, new AgendarSessaoRequest(slotId, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL)))
                .isInstanceOf(SlotIndisponivelException.class);
    }

    @Test
    void agendar_slotNoPassado_deveLancarSlotIndisponivelException() {
        slot.setInicio(LocalDateTime.now().minusDays(1));
        slot.setFim(LocalDateTime.now().minusDays(1).plusHours(1));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> sessaoService.agendar(
                        pacienteId, new AgendarSessaoRequest(slotId, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL)))
                .isInstanceOf(SlotIndisponivelException.class);
    }

    @Test
    void agendar_terapiaDeCasal_deveCalcularValorEmDobro() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(precificacaoService.calcularValorSessao(FaixaRenda.FAIXA_1, Modalidade.AVULSA, TipoAtendimento.CASAL))
                .thenReturn(new BigDecimal("120.00"));
        when(precificacaoService.calcularTaxa(new BigDecimal("120.00"))).thenReturn(new BigDecimal("24.00"));
        when(precificacaoService.calcularValorLiquido(new BigDecimal("120.00"), new BigDecimal("24.00")))
                .thenReturn(new BigDecimal("96.00"));
        when(precificacaoService.calcularValorPacoteTotal(FaixaRenda.FAIXA_1, TipoAtendimento.CASAL))
                .thenReturn(new BigDecimal("456.00"));
        when(precificacaoService.calcularEconomiaPacote(FaixaRenda.FAIXA_1, TipoAtendimento.CASAL))
                .thenReturn(new BigDecimal("24.00"));
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> {
            Sessao sessao = invocation.getArgument(0);
            sessao.setId(UUID.randomUUID());
            return sessao;
        });

        SessaoResponse resposta = sessaoService.agendar(
                pacienteId, new AgendarSessaoRequest(slotId, Modalidade.AVULSA, TipoAtendimento.CASAL));

        assertThat(resposta.valorSessao()).isEqualByComparingTo("120.00");
        assertThat(resposta.tipoAtendimento()).isEqualTo(TipoAtendimento.CASAL);
    }

    @Test
    void listar_deveRetornarSessoesDoPaciente() {
        Sessao sessao = Sessao.builder()
                .id(UUID.randomUUID())
                .slot(slot)
                .paciente(paciente)
                .psicologo(psicologo)
                .modalidade(Modalidade.AVULSA)
                .tipoAtendimento(TipoAtendimento.INDIVIDUAL)
                .valorSessao(new BigDecimal("60.00"))
                .status(StatusSessao.AGENDADA)
                .build();
        when(sessaoRepository.findByPacienteIdOrderByCriadaEmDesc(pacienteId)).thenReturn(List.of(sessao));
        when(precificacaoService.calcularValorSessao(FaixaRenda.FAIXA_1, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("60.00"));
        when(precificacaoService.calcularValorPacoteTotal(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("228.00"));
        when(precificacaoService.calcularEconomiaPacote(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("12.00"));

        List<SessaoResponse> respostas = sessaoService.listar(pacienteId);

        assertThat(respostas).hasSize(1);
        assertThat(respostas.get(0).nomePsicologo()).isEqualTo("Psicólogo Teste");
    }

    @Test
    void marcarRealizada_sessaoAgendadaDoPsicologo_deveMudarStatusEGerarCobranca() {
        UUID sessaoId = UUID.randomUUID();
        Sessao sessao = Sessao.builder()
                .id(sessaoId)
                .slot(slot)
                .paciente(paciente)
                .psicologo(psicologo)
                .modalidade(Modalidade.AVULSA)
                .tipoAtendimento(TipoAtendimento.INDIVIDUAL)
                .valorSessao(new BigDecimal("60.00"))
                .status(StatusSessao.AGENDADA)
                .build();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));
        when(precificacaoService.calcularValorSessao(FaixaRenda.FAIXA_1, Modalidade.AVULSA, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("60.00"));
        when(precificacaoService.calcularValorPacoteTotal(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("228.00"));
        when(precificacaoService.calcularEconomiaPacote(FaixaRenda.FAIXA_1, TipoAtendimento.INDIVIDUAL))
                .thenReturn(new BigDecimal("12.00"));

        SessaoResponse resposta = sessaoService.marcarRealizada(psicologo.getId(), sessaoId);

        assertThat(resposta.status()).isEqualTo(StatusSessao.REALIZADA);
        org.mockito.Mockito.verify(cobrancaService).gerar(sessao);
    }

    @Test
    void marcarRealizada_sessaoDeOutroPsicologo_deveLancarExcecao() {
        UUID sessaoId = UUID.randomUUID();
        Sessao sessao = Sessao.builder()
                .id(sessaoId)
                .slot(slot)
                .paciente(paciente)
                .psicologo(psicologo)
                .status(StatusSessao.AGENDADA)
                .build();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoService.marcarRealizada(UUID.randomUUID(), sessaoId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void marcarRealizada_sessaoJaRealizada_deveLancarExcecao() {
        UUID sessaoId = UUID.randomUUID();
        Sessao sessao = Sessao.builder()
                .id(sessaoId)
                .slot(slot)
                .paciente(paciente)
                .psicologo(psicologo)
                .status(StatusSessao.REALIZADA)
                .build();
        when(sessaoRepository.findById(sessaoId)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> sessaoService.marcarRealizada(psicologo.getId(), sessaoId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
