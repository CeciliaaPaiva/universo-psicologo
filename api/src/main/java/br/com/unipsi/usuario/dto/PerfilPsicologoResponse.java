package br.com.unipsi.usuario.dto;

import br.com.unipsi.usuario.domain.Psicologo;

public record PerfilPsicologoResponse(
        String nome,
        String crp,
        String especializacao,
        String politicaCancelamento,
        String linkVideochamada,
        String fotoUrl,
        boolean googleCalendarConectado) {

    public static PerfilPsicologoResponse from(Psicologo psicologo) {
        return new PerfilPsicologoResponse(
                psicologo.getUsuario().getNome(),
                psicologo.getCrp(),
                psicologo.getEspecializacao(),
                psicologo.getPoliticaCancelamento(),
                psicologo.getLinkVideochamada(),
                psicologo.getFotoUrl(),
                psicologo.getGoogleRefreshToken() != null);
    }
}
