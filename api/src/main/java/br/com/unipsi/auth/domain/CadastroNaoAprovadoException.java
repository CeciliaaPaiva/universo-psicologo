package br.com.unipsi.auth.domain;

import br.com.unipsi.usuario.domain.StatusAprovacao;

public class CadastroNaoAprovadoException extends RuntimeException {

    public CadastroNaoAprovadoException(StatusAprovacao status) {
        super(switch (status) {
            case PENDENTE_APROVACAO -> "Seu cadastro ainda está em análise pela nossa equipe.";
            case REPROVADO -> "Seu cadastro não foi aprovado. Verifique o e-mail enviado para mais detalhes.";
            case APROVADO -> "Cadastro aprovado.";
        });
    }
}
