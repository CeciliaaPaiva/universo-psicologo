package br.com.unipsi.mensagem.domain;

public class CanalMensagemFechadoException extends RuntimeException {

    public CanalMensagemFechadoException(String mensagem) {
        super(mensagem);
    }
}
