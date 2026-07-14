CREATE TABLE cobranca (
    id UUID PRIMARY KEY,
    sessao_id UUID NOT NULL UNIQUE REFERENCES sessao (id),
    valor_bruto NUMERIC(10, 2) NOT NULL,
    taxa_plataforma NUMERIC(10, 2) NOT NULL,
    valor_liquido NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    criada_em TIMESTAMP NOT NULL DEFAULT now(),
    paga_em TIMESTAMP,
    cancelada_em TIMESTAMP
);

CREATE INDEX idx_cobranca_sessao ON cobranca (sessao_id);
CREATE INDEX idx_cobranca_status ON cobranca (status);

ALTER TABLE sessao ADD COLUMN lembrete_24h_enviado BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE sessao ADD COLUMN lembrete_1h_enviado BOOLEAN NOT NULL DEFAULT false;
