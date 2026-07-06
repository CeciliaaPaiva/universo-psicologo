package br.com.unipsi.plantao.dto;

import br.com.unipsi.plantao.domain.DiaSemana;
import br.com.unipsi.plantao.domain.DisponibilidadePlantao;
import java.time.LocalDate;
import java.util.UUID;

public record DisponibilidadeResponse(UUID id, DiaSemana diaSemana, LocalDate dataEspecifica, boolean ativo) {

    public static DisponibilidadeResponse from(DisponibilidadePlantao disponibilidade) {
        return new DisponibilidadeResponse(
                disponibilidade.getId(),
                disponibilidade.getDiaSemana(),
                disponibilidade.getDataEspecifica(),
                disponibilidade.isAtivo());
    }
}
