package br.com.unipsi.plantao.dto;

import java.util.List;

public record StatusPlantaoResponse(boolean plantaoAtivoHoje, List<DisponibilidadeResponse> disponibilidades) {
}
