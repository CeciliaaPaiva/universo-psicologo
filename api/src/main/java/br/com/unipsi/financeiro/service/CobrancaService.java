package br.com.unipsi.financeiro.service;

import br.com.unipsi.agenda.domain.Sessao;
import br.com.unipsi.financeiro.domain.Cobranca;
import br.com.unipsi.financeiro.domain.StatusCobranca;
import br.com.unipsi.financeiro.dto.CobrancaResponse;
import br.com.unipsi.financeiro.dto.RelatorioFinanceiroResponse;
import br.com.unipsi.financeiro.repository.CobrancaRepository;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final EmailService emailService;
    private final NotificacaoService notificacaoService;

    @Transactional
    public Cobranca gerar(Sessao sessao) {
        Cobranca cobranca = cobrancaRepository.save(Cobranca.builder()
                .sessao(sessao)
                .valorBruto(sessao.getValorSessao())
                .taxaPlataforma(sessao.getTaxaPlataforma())
                .valorLiquido(sessao.getValorLiquido())
                .status(StatusCobranca.PENDENTE)
                .build());

        Paciente paciente = sessao.getPaciente();
        emailService.enviarCobrancaGerada(
                paciente.getUsuario().getEmail(), paciente.getUsuario().getNome(), sessao.getValorSessao());
        notificacaoService.criar(paciente.getId(),
                "Cobrança de R$ %s gerada pela sua sessão".formatted(sessao.getValorSessao()));

        return cobranca;
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponse> listarPorPaciente(UUID pacienteId) {
        return cobrancaRepository.findBySessaoPacienteIdOrderByCriadaEmDesc(pacienteId).stream()
                .map(CobrancaResponse::from)
                .toList();
    }

    @Transactional
    public CobrancaResponse pagar(UUID pacienteId, UUID cobrancaId) {
        Cobranca cobranca = buscarDoPaciente(pacienteId, cobrancaId);
        if (cobranca.getStatus() != StatusCobranca.PENDENTE) {
            throw new IllegalArgumentException("Apenas cobranças pendentes podem ser pagas");
        }

        cobranca.setStatus(StatusCobranca.PAGO);
        cobranca.setPagaEm(Instant.now());
        cobrancaRepository.save(cobranca);

        Psicologo psicologo = cobranca.getSessao().getPsicologo();
        emailService.enviarCobrancaPaga(
                psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome(), cobranca.getValorLiquido());
        notificacaoService.criar(psicologo.getId(),
                "Pagamento confirmado — você recebeu R$ %s".formatted(cobranca.getValorLiquido()));

        return CobrancaResponse.from(cobranca);
    }

    @Transactional
    public CobrancaResponse cancelar(UUID psicologoId, UUID cobrancaId) {
        Cobranca cobranca = buscarDoPsicologo(psicologoId, cobrancaId);
        if (cobranca.getStatus() != StatusCobranca.PENDENTE) {
            throw new IllegalArgumentException("Apenas cobranças pendentes podem ser canceladas");
        }

        cobranca.setStatus(StatusCobranca.CANCELADO);
        cobranca.setCanceladaEm(Instant.now());
        cobrancaRepository.save(cobranca);

        Paciente paciente = cobranca.getSessao().getPaciente();
        emailService.enviarCobrancaCancelada(paciente.getUsuario().getEmail(), paciente.getUsuario().getNome());
        notificacaoService.criar(paciente.getId(), "Uma cobrança pendente sua foi cancelada");

        return CobrancaResponse.from(cobranca);
    }

    @Transactional(readOnly = true)
    public RelatorioFinanceiroResponse relatorio(UUID psicologoId, LocalDate inicio, LocalDate fim) {
        Instant inicioInstant = inicio.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        Instant fimInstant = fim.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();

        List<CobrancaResponse> cobrancas = cobrancaRepository
                .buscarPagasPorPsicologoNoPeriodo(psicologoId, StatusCobranca.PAGO, inicioInstant, fimInstant)
                .stream()
                .map(CobrancaResponse::from)
                .toList();

        BigDecimal totalBruto = somar(cobrancas, CobrancaResponse::valorBruto);
        BigDecimal totalTaxa = somar(cobrancas, CobrancaResponse::taxaPlataforma);
        BigDecimal totalLiquido = somar(cobrancas, CobrancaResponse::valorLiquido);

        return new RelatorioFinanceiroResponse(totalBruto, totalTaxa, totalLiquido, cobrancas);
    }

    private BigDecimal somar(List<CobrancaResponse> cobrancas, java.util.function.Function<CobrancaResponse, BigDecimal> extrator) {
        return cobrancas.stream().map(extrator).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Cobranca buscarDoPaciente(UUID pacienteId, UUID cobrancaId) {
        Cobranca cobranca = cobrancaRepository.findById(cobrancaId)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada"));
        if (!cobranca.getSessao().getPaciente().getId().equals(pacienteId)) {
            throw new IllegalArgumentException("Cobrança não encontrada");
        }
        return cobranca;
    }

    private Cobranca buscarDoPsicologo(UUID psicologoId, UUID cobrancaId) {
        Cobranca cobranca = cobrancaRepository.findById(cobrancaId)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada"));
        if (!cobranca.getSessao().getPsicologo().getId().equals(psicologoId)) {
            throw new IllegalArgumentException("Cobrança não encontrada");
        }
        return cobranca;
    }
}
