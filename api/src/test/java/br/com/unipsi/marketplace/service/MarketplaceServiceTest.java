package br.com.unipsi.marketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.marketplace.dto.PsicologoPerfilResponse;
import br.com.unipsi.marketplace.dto.PsicologoResumoResponse;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
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
class MarketplaceServiceTest {

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private PrecificacaoService precificacaoService;

    @InjectMocks
    private MarketplaceService marketplaceService;

    private UUID pacienteId;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        paciente = Paciente.builder().id(pacienteId).faixaRenda(FaixaRenda.FAIXA_2).build();
    }

    @Test
    void buscar_deveRetornarApenasPsicologosComSlotsDisponiveisFuturos() {
        Psicologo comSlots = psicologoAprovado("Ana", "Ansiedade");
        Psicologo semSlots = psicologoAprovado("Bruno", "Luto");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findByStatusAprovacao(StatusAprovacao.APROVADO))
                .thenReturn(List.of(comSlots, semSlots));
        when(slotRepository.findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(eq(comSlots.getId()), any()))
                .thenReturn(List.of(slotFuturo(comSlots)));
        when(slotRepository.findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(eq(semSlots.getId()), any()))
                .thenReturn(List.of());
        when(precificacaoService.calcularValorSessao(any(), any())).thenReturn(java.math.BigDecimal.TEN);

        List<PsicologoResumoResponse> resultado = marketplaceService.buscar(pacienteId, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(comSlots.getId());
    }

    @Test
    void buscar_deveFiltrarPorAreaDeAtuacaoIgnorandoCaixa() {
        Psicologo ansiedade = psicologoAprovado("Ana", "TCC", "Ansiedade");
        Psicologo luto = psicologoAprovado("Bruno", "Psicanálise", "Luto");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findByStatusAprovacao(StatusAprovacao.APROVADO))
                .thenReturn(List.of(ansiedade, luto));
        when(slotRepository.findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(eq(ansiedade.getId()), any()))
                .thenReturn(List.of(slotFuturo(ansiedade)));
        when(precificacaoService.calcularValorSessao(any(), any())).thenReturn(java.math.BigDecimal.TEN);

        List<PsicologoResumoResponse> resultado = marketplaceService.buscar(pacienteId, "ANSI");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Ana");
    }

    @Test
    void perfil_deveLancarExcecaoQuandoPsicologoNaoAprovado() {
        UUID psicologoId = UUID.randomUUID();
        Psicologo pendente = Psicologo.builder()
                .id(psicologoId)
                .statusAprovacao(StatusAprovacao.PENDENTE_APROVACAO)
                .build();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(pendente));

        assertThatThrownBy(() -> marketplaceService.perfil(pacienteId, psicologoId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perfil_deveRetornarSlotsDisponiveisEValoresCalculados() {
        Psicologo psicologo = psicologoAprovado("Ana", "Ansiedade");
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(psicologoRepository.findById(psicologo.getId())).thenReturn(Optional.of(psicologo));
        when(slotRepository.findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(eq(psicologo.getId()), any()))
                .thenReturn(List.of(slotFuturo(psicologo)));
        when(precificacaoService.calcularValorSessao(any(), any())).thenReturn(java.math.BigDecimal.TEN);

        PsicologoPerfilResponse resposta = marketplaceService.perfil(pacienteId, psicologo.getId());

        assertThat(resposta.slotsDisponiveis()).hasSize(1);
        assertThat(resposta.nome()).isEqualTo("Ana");
    }

    private Psicologo psicologoAprovado(String nome, String especializacao) {
        return psicologoAprovado(nome, especializacao, "Geral");
    }

    private Psicologo psicologoAprovado(String nome, String especializacao, String... areasAtuacao) {
        Usuario usuario = Usuario.builder().nome(nome).build();
        return Psicologo.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .especializacao(especializacao)
                .areasAtuacao(List.of(areasAtuacao))
                .statusAprovacao(StatusAprovacao.APROVADO)
                .build();
    }

    private Slot slotFuturo(Psicologo psicologo) {
        return Slot.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologo)
                .inicio(LocalDateTime.now().plusDays(1))
                .fim(LocalDateTime.now().plusDays(1).plusHours(1))
                .disponivel(true)
                .build();
    }
}
