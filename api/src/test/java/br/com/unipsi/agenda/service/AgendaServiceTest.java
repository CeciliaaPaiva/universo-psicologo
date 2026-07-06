package br.com.unipsi.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.agenda.dto.CriarSlotRequest;
import br.com.unipsi.agenda.dto.SlotResponse;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.usuario.domain.Psicologo;
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
class AgendaServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private GoogleCalendarService googleCalendarService;

    @InjectMocks
    private AgendaService agendaService;

    private UUID psicologoId;
    private Psicologo psicologo;

    @BeforeEach
    void setUp() {
        psicologoId = UUID.randomUUID();
        psicologo = Psicologo.builder().id(psicologoId).build();
    }

    @Test
    void criarSlots_deveCriarSlotDisponivelESincronizarComGoogle() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fim = inicio.plusHours(1);

        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(slotRepository.existsByPsicologoIdAndInicioLessThanAndFimGreaterThan(psicologoId, fim, inicio))
                .thenReturn(false);
        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> {
            Slot slot = invocation.getArgument(0);
            slot.setId(UUID.randomUUID());
            return slot;
        });
        when(googleCalendarService.criarEvento(any(), any())).thenReturn("google-event-1");

        List<SlotResponse> respostas = agendaService.criarSlots(psicologoId, List.of(new CriarSlotRequest(inicio, fim)));

        assertThat(respostas).hasSize(1);
        assertThat(respostas.get(0).disponivel()).isTrue();
        assertThat(respostas.get(0).sincronizadoGoogleCalendar()).isTrue();
    }

    @Test
    void criarSlots_deveLancarExcecaoQuandoFimNaoEDepoisDoInicio() {
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.minusMinutes(30);

        assertThatThrownBy(() -> agendaService.criarSlots(psicologoId, List.of(new CriarSlotRequest(inicio, fim))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void criarSlots_deveLancarExcecaoQuandoJaExisteSlotNoIntervalo() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);

        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(slotRepository.existsByPsicologoIdAndInicioLessThanAndFimGreaterThan(psicologoId, fim, inicio))
                .thenReturn(true);

        assertThatThrownBy(() -> agendaService.criarSlots(psicologoId, List.of(new CriarSlotRequest(inicio, fim))))
                .isInstanceOf(SlotIndisponivelException.class);
    }

    @Test
    void cancelar_deveRemoverSlotERemoverEventoDoGoogle() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .id(slotId)
                .psicologo(psicologo)
                .inicio(LocalDateTime.now().plusDays(1))
                .fim(LocalDateTime.now().plusDays(1).plusHours(1))
                .disponivel(true)
                .googleEventId("google-event-1")
                .build();
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        agendaService.cancelar(psicologoId, slotId, "Imprevisto");

        verify(googleCalendarService).removerEvento(psicologo, "google-event-1");
        verify(slotRepository).delete(slot);
    }

    @Test
    void cancelar_deveLancarExcecaoQuandoSlotPertenceAOutroPsicologo() {
        UUID slotId = UUID.randomUUID();
        Psicologo outroPsicologo = Psicologo.builder().id(UUID.randomUUID()).build();
        Slot slot = Slot.builder().id(slotId).psicologo(outroPsicologo).build();
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> agendaService.cancelar(psicologoId, slotId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
