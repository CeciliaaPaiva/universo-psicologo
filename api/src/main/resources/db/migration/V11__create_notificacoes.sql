CREATE TABLE notificacao (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario (id),
    mensagem TEXT NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT false,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificacao_usuario ON notificacao (usuario_id, criada_em DESC);
