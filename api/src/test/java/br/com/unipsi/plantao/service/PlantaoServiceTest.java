package br.com.unipsi.plantao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.plantao.domain.DiaSemana;
import br.com.unipsi.plantao.domain.DisponibilidadePlantao;
import br.com.unipsi.plantao.dto.CriarDisponibilidadeRequest;
import br.com.unipsi.plantao.dto.DisponibilidadeResponse;
import br.com.unipsi.plantao.dto.StatusPlantaoResponse;
import br.com.unipsi.plantao.repository.DisponibilidadePlantaoRepository;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
class PlantaoServiceTest {

    @Mock
    private DisponibilidadePlantaoRepository disponibilidadePlantaoRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @InjectMocks
    private PlantaoService plantaoService;

    private UUID psicologoId;
    private Psicologo psicologo;

    @BeforeEach
    void setUp() {
        psicologoId = UUID.randomUUID();
        psicologo = Psicologo.builder().id(psicologoId).build();
    }

    @Test
    void criar_deveLancarExcecaoSemDiaDaSemanaOuDataEspecifica() {
        assertThatThrownBy(() -> plantaoService.criar(psicologoId, new CriarDisponibilidadeRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void criar_deveSalvarDisponibilidadeAtiva() {
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(disponibilidadePlantaoRepository.save(any(DisponibilidadePlantao.class)))
                .thenAnswer(invocation -> {
                    DisponibilidadePlantao disponibilidade = invocation.getArgument(0);
                    disponibilidade.setId(UUID.randomUUID());
                    return disponibilidade;
                });

        DisponibilidadeResponse resposta =
                plantaoService.criar(psicologoId, new CriarDisponibilidadeRequest(DiaSemana.SEG, null));

        assertThat(resposta.ativo()).isTrue();
        assertThat(resposta.diaSemana()).isEqualTo(DiaSemana.SEG);
    }

    @Test
    void status_deveIndicarAtivoHojeQuandoDiaDaSemanaAtualEstaAtivo() {
        DiaSemana hoje = DiaSemana.from(LocalDate.now().getDayOfWeek());
        DisponibilidadePlantao disponibilidade = DisponibilidadePlantao.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologo)
                .diaSemana(hoje)
                .ativo(true)
                .build();
        when(disponibilidadePlantaoRepository.findByPsicologoIdOrderByCriadoEmDesc(psicologoId))
                .thenReturn(List.of(disponibilidade));

        StatusPlantaoResponse status = plantaoService.status(psicologoId);

        assertThat(status.plantaoAtivoHoje()).isTrue();
    }

    @Test
    void status_deveIndicarInativoQuandoNenhumaDisponibilidadeCobreHoje() {
        DiaSemana diaSemanaDiferente =
                DiaSemana.from(LocalDate.now().getDayOfWeek()) == DiaSemana.DOM ? DiaSemana.SEG : DiaSemana.DOM;
        DisponibilidadePlantao disponibilidade = DisponibilidadePlantao.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologo)
                .diaSemana(diaSemanaDiferente)
                .ativo(true)
                .build();
        when(disponibilidadePlantaoRepository.findByPsicologoIdOrderByCriadoEmDesc(psicologoId))
                .thenReturn(List.of(disponibilidade));

        StatusPlantaoResponse status = plantaoService.status(psicologoId);

        assertThat(status.plantaoAtivoHoje()).isFalse();
    }

    @Test
    void ativar_deveLancarExcecaoQuandoDisponibilidadePertenceAOutroPsicologo() {
        UUID disponibilidadeId = UUID.randomUUID();
        Psicologo outroPsicologo = Psicologo.builder().id(UUID.randomUUID()).build();
        DisponibilidadePlantao disponibilidade =
                DisponibilidadePlantao.builder().id(disponibilidadeId).psicologo(outroPsicologo).build();
        when(disponibilidadePlantaoRepository.findById(disponibilidadeId)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> plantaoService.ativar(psicologoId, disponibilidadeId, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ativar_deveDesativarDisponibilidadeDoProprioPsicologo() {
        UUID disponibilidadeId = UUID.randomUUID();
        DisponibilidadePlantao disponibilidade = DisponibilidadePlantao.builder()
                .id(disponibilidadeId)
                .psicologo(psicologo)
                .ativo(true)
                .build();
        when(disponibilidadePlantaoRepository.findById(disponibilidadeId)).thenReturn(Optional.of(disponibilidade));
        when(disponibilidadePlantaoRepository.save(any(DisponibilidadePlantao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        plantaoService.ativar(psicologoId, disponibilidadeId, false);

        assertThat(disponibilidade.isAtivo()).isFalse();
    }

    @Test
    void buscarPsicologosDePlantaoHoje_deveRetornarApenasAtivosCobrindoHoje() {
        DiaSemana hoje = DiaSemana.from(LocalDate.now().getDayOfWeek());
        DiaSemana diaDiferente = hoje == DiaSemana.DOM ? DiaSemana.SEG : DiaSemana.DOM;

        Psicologo psicologoAtivoHoje = Psicologo.builder().id(UUID.randomUUID()).build();
        Psicologo psicologoOutroDia = Psicologo.builder().id(UUID.randomUUID()).build();

        DisponibilidadePlantao disponivelHoje = DisponibilidadePlantao.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologoAtivoHoje)
                .diaSemana(hoje)
                .ativo(true)
                .build();
        DisponibilidadePlantao disponivelOutroDia = DisponibilidadePlantao.builder()
                .id(UUID.randomUUID())
                .psicologo(psicologoOutroDia)
                .diaSemana(diaDiferente)
                .ativo(true)
                .build();
        when(disponibilidadePlantaoRepository.findByAtivoTrue())
                .thenReturn(List.of(disponivelHoje, disponivelOutroDia));

        List<Psicologo> psicologos = plantaoService.buscarPsicologosDePlantaoHoje();

        assertThat(psicologos).containsExactly(psicologoAtivoHoje);
    }

    @Test
    void buscarPsicologosDePlantaoHoje_semNenhumAtivoHoje_deveRetornarListaVazia() {
        when(disponibilidadePlantaoRepository.findByAtivoTrue()).thenReturn(List.of());

        List<Psicologo> psicologos = plantaoService.buscarPsicologosDePlantaoHoje();

        assertThat(psicologos).isEmpty();
    }
}
