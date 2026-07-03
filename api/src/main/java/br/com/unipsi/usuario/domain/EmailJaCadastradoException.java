package br.com.unipsi.usuario.domain;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}
