package br.com.unipsi.agenda.service;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.dto.AgendarSessaoRequest;
import br.com.unipsi.agenda.dto.SessaoResponse;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.marketplace.service.PrecificacaoService;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SlotRepository slotRepository;
    private final SessaoRepository sessaoRepository;
    private final PacienteRepository pacienteRepository;
    private final PrecificacaoService precificacaoService;
    private final EmailService emailService;

    @Transactional
    public SessaoResponse agendar(UUID pacienteId, AgendarSessaoRequest pedido) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        Slot slot = slotRepository.findById(pedido.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot não encontrado"));

        if (!slot.isDisponivel() || !slot.getInicio().isAfter(LocalDateTime.now())) {
            throw new SlotIndisponivelException("Este horário não está mais disponível");
        }

        BigDecimal valorSessao = precificacaoService.calcularValorSessao(paciente.getFaixaRenda(), pedido.modalidade());
        BigDecimal taxaPlataforma = precificacaoService.calcularTaxa(valorSessao);
        BigDecimal valorLiquido = precificacaoService.calcularValorLiquido(valorSessao, taxaPlataforma);

        slot.setDisponivel(false);
        slotRepository.save(slot);

        Sessao sessao = sessaoRepository.save(Sessao.builder()
                .slot(slot)
                .paciente(paciente)
                .psicologo(slot.getPsicologo())
                .modalidade(pedido.modalidade())
                .valorSessao(valorSessao)
                .taxaPlataforma(taxaPlataforma)
                .valorLiquido(valorLiquido)
                .status(StatusSessao.AGENDADA)
                .build());

        notificarAgendamento(sessao);

        return SessaoResponse.from(sessao);
    }

    @Transactional(readOnly = true)
    public List<SessaoResponse> listar(UUID pacienteId) {
        return sessaoRepository.findByPacienteIdOrderByCriadaEmDesc(pacienteId).stream()
                .map(SessaoResponse::from)
                .toList();
    }

    private void notificarAgendamento(Sessao sessao) {
        Psicologo psicologo = sessao.getPsicologo();
        Paciente paciente = sessao.getPaciente();
        LocalDateTime inicio = sessao.getSlot().getInicio();

        emailService.enviarConfirmacaoAgendamentoPaciente(
                paciente.getUsuario().getEmail(),
                paciente.getUsuario().getNome(),
                psicologo.getUsuario().getNome(),
                inicio,
                psicologo.getLinkVideochamada());

        emailService.enviarConfirmacaoAgendamentoPsicologo(
                psicologo.getUsuario().getEmail(),
                psicologo.getUsuario().getNome(),
                paciente.getUsuario().getNome(),
                inicio,
                psicologo.getLinkVideochamada());
    }
}
