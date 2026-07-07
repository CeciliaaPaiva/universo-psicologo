package br.com.unipsi.usuario.dto;

import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;

public record PerfilPacienteResponse(String nome, String email, FaixaRenda faixaRenda) {

    public static PerfilPacienteResponse from(Paciente paciente) {
        return new PerfilPacienteResponse(
                paciente.getUsuario().getNome(), paciente.getUsuario().getEmail(), paciente.getFaixaRenda());
    }
}
