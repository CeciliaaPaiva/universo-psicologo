package br.com.unipsi.chatbot.domain;

public class RateLimitExcedidoException extends RuntimeException {

    public RateLimitExcedidoException(String mensagem) {
        super(mensagem);
    }
}
