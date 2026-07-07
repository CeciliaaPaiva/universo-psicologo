package br.com.unipsi.prontuario.domain;

public class AcessoProntuarioNegadoException extends RuntimeException {

    public AcessoProntuarioNegadoException(String mensagem) {
        super(mensagem);
    }
}
