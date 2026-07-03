package br.com.unipsi.usuario.domain;

public class PacienteNaoElegivelException extends RuntimeException {

    public PacienteNaoElegivelException(String mensagem) {
        super(mensagem);
    }
}
