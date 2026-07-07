package br.com.unipsi.marketplace.service;

import br.com.unipsi.agenda.domain.Modalidade;
import br.com.unipsi.agenda.domain.Slot;
import br.com.unipsi.agenda.dto.SlotResponse;
import br.com.unipsi.agenda.repository.SlotRepository;
import br.com.unipsi.marketplace.dto.PsicologoPerfilResponse;
import br.com.unipsi.marketplace.dto.PsicologoResumoResponse;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final PsicologoRepository psicologoRepository;
    private final PacienteRepository pacienteRepository;
    private final SlotRepository slotRepository;
    private final PrecificacaoService precificacaoService;

    @Transactional(readOnly = true)
    public List<PsicologoResumoResponse> buscar(UUID pacienteId, String especialidade) {
        FaixaRenda faixaRenda = buscarPaciente(pacienteId).getFaixaRenda();

        return psicologoRepository.findByStatusAprovacao(StatusAprovacao.APROVADO).stream()
                .filter(psicologo -> corresponde(psicologo, especialidade))
                .map(psicologo -> paraResumo(psicologo, faixaRenda))
                .filter(resumo -> !resumo.proximasDisponibilidades().isEmpty())
                .toList();
    }

    @Transactional(readOnly = true)
    public PsicologoPerfilResponse perfil(UUID pacienteId, UUID psicologoId) {
        FaixaRenda faixaRenda = buscarPaciente(pacienteId).getFaixaRenda();
        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .filter(p -> p.getStatusAprovacao() == StatusAprovacao.APROVADO)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        List<SlotResponse> slots = slotsDisponiveis(psicologoId).stream()
                .map(SlotResponse::from)
                .toList();

        return new PsicologoPerfilResponse(
                psicologo.getId(),
                psicologo.getUsuario().getNome(),
                psicologo.getEspecializacao(),
                psicologo.getPoliticaCancelamento(),
                psicologo.getLinkVideochamada(),
                psicologo.getFotoUrl(),
                precificacaoService.calcularValorSessao(faixaRenda, Modalidade.AVULSA),
                precificacaoService.calcularValorSessao(faixaRenda, Modalidade.PACOTE_MENSAL),
                slots);
    }

    private PsicologoResumoResponse paraResumo(Psicologo psicologo, FaixaRenda faixaRenda) {
        List<LocalDateTime> proximas = slotsDisponiveis(psicologo.getId()).stream()
                .map(Slot::getInicio)
                .limit(3)
                .toList();

        return new PsicologoResumoResponse(
                psicologo.getId(),
                psicologo.getUsuario().getNome(),
                psicologo.getEspecializacao(),
                psicologo.getFotoUrl(),
                proximas,
                precificacaoService.calcularValorSessao(faixaRenda, Modalidade.AVULSA),
                precificacaoService.calcularValorSessao(faixaRenda, Modalidade.PACOTE_MENSAL));
    }

    private List<Slot> slotsDisponiveis(UUID psicologoId) {
        return slotRepository.findByPsicologoIdAndDisponivelTrueAndInicioAfterOrderByInicio(
                psicologoId, LocalDateTime.now());
    }

    private boolean corresponde(Psicologo psicologo, String especialidade) {
        return especialidade == null
                || especialidade.isBlank()
                || (psicologo.getEspecializacao() != null
                        && psicologo.getEspecializacao().toLowerCase().contains(especialidade.toLowerCase()));
    }

    private Paciente buscarPaciente(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));
    }
}
