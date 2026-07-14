package br.com.unipsi.usuario.dto;

import java.util.List;

public record AtualizarPerfilPsicologoRequest(
        String especializacao, String politicaCancelamento, String linkVideochamada, List<String> areasAtuacao) {
}
