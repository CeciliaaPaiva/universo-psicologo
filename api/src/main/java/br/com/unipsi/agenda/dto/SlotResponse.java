package br.com.unipsi.agenda.dto;

import br.com.unipsi.agenda.domain.Slot;
import java.time.LocalDateTime;
import java.util.UUID;

public record SlotResponse(
        UUID id, LocalDateTime inicio, LocalDateTime fim, boolean disponivel, boolean sincronizadoGoogleCalendar) {

    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getInicio(),
                slot.getFim(),
                slot.isDisponivel(),
                slot.getGoogleEventId() != null);
    }
}
