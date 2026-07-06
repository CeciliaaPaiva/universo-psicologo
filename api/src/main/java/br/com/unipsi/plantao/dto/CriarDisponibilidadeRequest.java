package br.com.unipsi.plantao.dto;

import br.com.unipsi.plantao.domain.DiaSemana;
import java.time.LocalDate;

public record CriarDisponibilidadeRequest(DiaSemana diaSemana, LocalDate dataEspecifica) {
}
