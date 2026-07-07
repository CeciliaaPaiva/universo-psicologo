CREATE TABLE sessao (
    id UUID PRIMARY KEY,
    slot_id UUID NOT NULL UNIQUE REFERENCES slot (id),
    paciente_id UUID NOT NULL REFERENCES paciente (id),
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    modalidade VARCHAR(20) NOT NULL,
    valor_sessao NUMERIC(10, 2) NOT NULL,
    taxa_plataforma NUMERIC(10, 2) NOT NULL,
    valor_liquido NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    cancelado_em TIMESTAMP,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sessao_paciente ON sessao (paciente_id, criada_em);
CREATE INDEX idx_sessao_psicologo ON sessao (psicologo_id, criada_em);
