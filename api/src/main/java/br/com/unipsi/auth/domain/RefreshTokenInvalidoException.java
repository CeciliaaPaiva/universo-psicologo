package br.com.unipsi.auth.domain;

public class RefreshTokenInvalidoException extends RuntimeException {

    public RefreshTokenInvalidoException() {
        super("Refresh token inválido ou expirado");
    }
}
