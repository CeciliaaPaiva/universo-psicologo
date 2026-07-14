package br.com.unipsi.usuario.dto;

import br.com.unipsi.usuario.domain.Psicologo;
import java.util.List;

public record PerfilPsicologoResponse(
        String nome,
        String crp,
        String especializacao,
        List<String> areasAtuacao,
        String politicaCancelamento,
        String linkVideochamada,
        String fotoUrl,
        boolean googleCalendarConectado) {

    public static PerfilPsicologoResponse from(Psicologo psicologo) {
        return new PerfilPsicologoResponse(
                psicologo.getUsuario().getNome(),
                psicologo.getCrp(),
                psicologo.getEspecializacao(),
                psicologo.getAreasAtuacao(),
                psicologo.getPoliticaCancelamento(),
                psicologo.getLinkVideochamada(),
                psicologo.getFotoUrl(),
                psicologo.getGoogleRefreshToken() != null);
    }
}
