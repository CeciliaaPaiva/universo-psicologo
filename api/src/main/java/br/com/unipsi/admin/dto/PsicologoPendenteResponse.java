package br.com.unipsi.admin.dto;

import br.com.unipsi.usuario.domain.Psicologo;
import java.time.Instant;
import java.util.UUID;

public record PsicologoPendenteResponse(
        UUID id,
        String nome,
        String email,
        String crp,
        String especializacao,
        String curriculoUrl,
        String politicaCancelamento,
        Instant criadoEm) {

    public static PsicologoPendenteResponse from(Psicologo psicologo) {
        return new PsicologoPendenteResponse(
                psicologo.getId(),
                psicologo.getUsuario().getNome(),
                psicologo.getUsuario().getEmail(),
                psicologo.getCrp(),
                psicologo.getEspecializacao(),
                psicologo.getCurriculoUrl(),
                psicologo.getPoliticaCancelamento(),
                psicologo.getUsuario().getCriadoEm());
    }
}
