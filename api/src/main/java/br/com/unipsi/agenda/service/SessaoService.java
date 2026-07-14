package br.com.unipsi.agenda.service;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.dto.AgendarSessaoRequest;
import br.com.unipsi.agenda.dto.SessaoResponse;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.financeiro.service.CobrancaService;
import br.com.unipsi.marketplace.service.PrecificacaoService;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");

    private final SlotRepository slotRepository;
    private final SessaoRepository sessaoRepository;
    private final PacienteRepository pacienteRepository;
    private final PrecificacaoService precificacaoService;
    private final EmailService emailService;
    private final CobrancaService cobrancaService;
    private final NotificacaoService notificacaoService;

    @Transactional
    public SessaoResponse agendar(UUID pacienteId, AgendarSessaoRequest pedido) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        Slot slot = slotRepository.findById(pedido.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot não encontrado"));

        if (!slot.isDisponivel() || !slot.getInicio().isAfter(LocalDateTime.now())) {
            throw new SlotIndisponivelException("Este horário não está mais disponível");
        }

        BigDecimal valorSessao = precificacaoService.calcularValorSessao(
                paciente.getFaixaRenda(), pedido.modalidade(), pedido.tipoAtendimento());
        BigDecimal taxaPlataforma = precificacaoService.calcularTaxa(valorSessao);
        BigDecimal valorLiquido = precificacaoService.calcularValorLiquido(valorSessao, taxaPlataforma);

        slot.setDisponivel(false);
        slotRepository.save(slot);

        Sessao sessao = sessaoRepository.save(Sessao.builder()
                .slot(slot)
                .paciente(paciente)
                .psicologo(slot.getPsicologo())
                .modalidade(pedido.modalidade())
                .tipoAtendimento(pedido.tipoAtendimento())
                .valorSessao(valorSessao)
                .taxaPlataforma(taxaPlataforma)
                .valorLiquido(valorLiquido)
                .status(StatusSessao.AGENDADA)
                .build());

        notificarAgendamento(sessao);

        return paraResposta(sessao);
    }

    @Transactional(readOnly = true)
    public List<SessaoResponse> listar(UUID pacienteId) {
        return sessaoRepository.findByPacienteIdOrderByCriadaEmDesc(pacienteId).stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessaoResponse> listarPorPsicologo(UUID psicologoId) {
        return sessaoRepository.findByPsicologoIdOrderByCriadaEmDesc(psicologoId).stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional
    public SessaoResponse marcarRealizada(UUID psicologoId, UUID sessaoId) {
        Sessao sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada"));
        if (!sessao.getPsicologo().getId().equals(psicologoId)) {
            throw new IllegalArgumentException("Sessão não encontrada");
        }
        if (sessao.getStatus() != StatusSessao.AGENDADA) {
            throw new IllegalArgumentException("Apenas sessões agendadas podem ser marcadas como realizadas");
        }

        sessao.setStatus(StatusSessao.REALIZADA);
        sessaoRepository.save(sessao);
        cobrancaService.gerar(sessao);

        return paraResposta(sessao);
    }

    private SessaoResponse paraResposta(Sessao sessao) {
        var faixaRenda = sessao.getPaciente().getFaixaRenda();
        var tipoAtendimento = sessao.getTipoAtendimento();
        BigDecimal valorSessaoAvulsa =
                precificacaoService.calcularValorSessao(faixaRenda, Modalidade.AVULSA, tipoAtendimento);
        BigDecimal valorPacoteTotal = precificacaoService.calcularValorPacoteTotal(faixaRenda, tipoAtendimento);
        BigDecimal economiaPacote = precificacaoService.calcularEconomiaPacote(faixaRenda, tipoAtendimento);
        return SessaoResponse.from(sessao, valorSessaoAvulsa, valorPacoteTotal, economiaPacote);
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

        notificacaoService.criar(paciente.getId(),
                "Sua sessão com %s foi agendada para %s".formatted(
                        psicologo.getUsuario().getNome(), inicio.format(FORMATO_DATA_HORA)));
        notificacaoService.criar(psicologo.getId(),
                "Nova sessão agendada com %s para %s".formatted(
                        paciente.getUsuario().getNome(), inicio.format(FORMATO_DATA_HORA)));
    }
}
