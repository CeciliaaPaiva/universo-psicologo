package br.com.unipsi.agenda.service;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.dto.CriarSlotRequest;
import br.com.unipsi.agenda.dto.SlotResponse;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private static final Logger log = LoggerFactory.getLogger(AgendaService.class);

    private final SlotRepository slotRepository;
    private final PsicologoRepository psicologoRepository;
    private final GoogleCalendarService googleCalendarService;
    private final SessaoRepository sessaoRepository;
    private final EmailService emailService;

    @Transactional
    public List<SlotResponse> criarSlots(UUID psicologoId, List<CriarSlotRequest> pedidos) {
        Psicologo psicologo = buscarPsicologo(psicologoId);

        return pedidos.stream()
                .map(pedido -> criarSlot(psicologo, pedido))
                .map(SlotResponse::from)
                .toList();
    }

    private Slot criarSlot(Psicologo psicologo, CriarSlotRequest pedido) {
        if (!pedido.fim().isAfter(pedido.inicio())) {
            throw new IllegalArgumentException("Horário de fim deve ser depois do horário de início");
        }
        if (slotRepository.existsByPsicologoIdAndInicioLessThanAndFimGreaterThan(
                psicologo.getId(), pedido.fim(), pedido.inicio())) {
            throw new SlotIndisponivelException("Já existe um slot cadastrado nesse intervalo de horário");
        }

        Slot slot = slotRepository.save(Slot.builder()
                .psicologo(psicologo)
                .inicio(pedido.inicio())
                .fim(pedido.fim())
                .disponivel(true)
                .build());

        String googleEventId = googleCalendarService.criarEvento(psicologo, slot);
        if (googleEventId != null) {
            slot.setGoogleEventId(googleEventId);
        }
        return slot;
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listar(UUID psicologoId, LocalDateTime inicio, LocalDateTime fim) {
        List<Slot> slots = (inicio != null && fim != null)
                ? slotRepository.findByPsicologoIdAndInicioBetweenOrderByInicio(psicologoId, inicio, fim)
                : slotRepository.findByPsicologoIdOrderByInicio(psicologoId);
        return slots.stream().map(SlotResponse::from).toList();
    }

    @Transactional
    public void cancelar(UUID psicologoId, UUID slotId, String motivo) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot não encontrado"));
        if (!slot.getPsicologo().getId().equals(psicologoId)) {
            throw new IllegalArgumentException("Slot não encontrado");
        }

        googleCalendarService.removerEvento(slot.getPsicologo(), slot.getGoogleEventId());
        if (motivo != null && !motivo.isBlank()) {
            log.info("Slot {} cancelado pelo psicólogo {}. Motivo: {}", slotId, psicologoId, motivo);
        }

        sessaoRepository.findBySlotIdAndStatus(slotId, StatusSessao.AGENDADA).ifPresentOrElse(
                sessao -> cancelarSessaoVinculada(sessao, motivo),
                () -> slotRepository.delete(slot));
    }

    private void cancelarSessaoVinculada(Sessao sessao, String motivo) {
        sessao.setStatus(StatusSessao.CANCELADA);
        sessao.setCanceladoEm(Instant.now());
        sessaoRepository.save(sessao);

        Paciente paciente = sessao.getPaciente();
        Psicologo psicologo = sessao.getPsicologo();
        emailService.enviarCancelamentoSessao(
                paciente.getUsuario().getEmail(),
                paciente.getUsuario().getNome(),
                psicologo.getUsuario().getNome(),
                sessao.getSlot().getInicio(),
                motivo);
        // O slot em si não é removido quando há uma sessão vinculada (FK sessao.slot_id):
        // permanece indisponível como registro histórico da sessão cancelada.
    }

    private Psicologo buscarPsicologo(UUID psicologoId) {
        return psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));
    }
}
