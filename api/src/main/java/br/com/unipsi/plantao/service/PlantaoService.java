package br.com.unipsi.plantao.service;

import br.com.unipsi.plantao.domain.DiaSemana;
import br.com.unipsi.plantao.domain.DisponibilidadePlantao;
import br.com.unipsi.plantao.dto.CriarDisponibilidadeRequest;
import br.com.unipsi.plantao.dto.DisponibilidadeResponse;
import br.com.unipsi.plantao.dto.StatusPlantaoResponse;
import br.com.unipsi.plantao.repository.DisponibilidadePlantaoRepository;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlantaoService {

    private final DisponibilidadePlantaoRepository disponibilidadePlantaoRepository;
    private final PsicologoRepository psicologoRepository;

    @Transactional
    public DisponibilidadeResponse criar(UUID psicologoId, CriarDisponibilidadeRequest pedido) {
        if (pedido.diaSemana() == null && pedido.dataEspecifica() == null) {
            throw new IllegalArgumentException("Informe um dia da semana ou uma data específica");
        }

        var psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        DisponibilidadePlantao disponibilidade = disponibilidadePlantaoRepository.save(DisponibilidadePlantao.builder()
                .psicologo(psicologo)
                .diaSemana(pedido.diaSemana())
                .dataEspecifica(pedido.dataEspecifica())
                .ativo(true)
                .build());

        return DisponibilidadeResponse.from(disponibilidade);
    }

    @Transactional(readOnly = true)
    public StatusPlantaoResponse status(UUID psicologoId) {
        List<DisponibilidadePlantao> disponibilidades =
                disponibilidadePlantaoRepository.findByPsicologoIdOrderByCriadoEmDesc(psicologoId);

        boolean ativoHoje = disponibilidades.stream().anyMatch(this::estaAtivoHoje);

        return new StatusPlantaoResponse(
                ativoHoje, disponibilidades.stream().map(DisponibilidadeResponse::from).toList());
    }

    @Transactional
    public void ativar(UUID psicologoId, UUID disponibilidadeId, boolean ativo) {
        DisponibilidadePlantao disponibilidade = disponibilidadePlantaoRepository.findById(disponibilidadeId)
                .orElseThrow(() -> new IllegalArgumentException("Disponibilidade de plantão não encontrada"));
        if (!disponibilidade.getPsicologo().getId().equals(psicologoId)) {
            throw new IllegalArgumentException("Disponibilidade de plantão não encontrada");
        }
        disponibilidade.setAtivo(ativo);
        disponibilidadePlantaoRepository.save(disponibilidade);
    }

    /**
     * Usado pelo chatbot ao identificar uma situação de crise: busca todos os psicólogos com
     * disponibilidade de plantão ativa cobrindo o dia de hoje (dia da semana ou data específica).
     */
    @Transactional(readOnly = true)
    public List<Psicologo> buscarPsicologosDePlantaoHoje() {
        return disponibilidadePlantaoRepository.findByAtivoTrue().stream()
                .filter(this::estaAtivoHoje)
                .map(DisponibilidadePlantao::getPsicologo)
                .distinct()
                .toList();
    }

    private boolean estaAtivoHoje(DisponibilidadePlantao disponibilidade) {
        LocalDate hoje = LocalDate.now();
        DiaSemana diaSemanaHoje = DiaSemana.from(hoje.getDayOfWeek());
        return disponibilidade.isAtivo()
                && (disponibilidade.getDiaSemana() == diaSemanaHoje || hoje.equals(disponibilidade.getDataEspecifica()));
    }
}
