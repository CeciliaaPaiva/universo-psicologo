package br.com.unipsi.agenda.domain;

public class SlotIndisponivelException extends RuntimeException {

    public SlotIndisponivelException(String message) {
        super(message);
    }
}
