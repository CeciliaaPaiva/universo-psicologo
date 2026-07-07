CREATE TABLE prontuario (
    id UUID PRIMARY KEY,
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    paciente_id UUID REFERENCES paciente (id),
    codinome VARCHAR(100) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_prontuario_psicologo_codinome UNIQUE (psicologo_id, codinome)
);

CREATE INDEX idx_prontuario_psicologo ON prontuario (psicologo_id);

CREATE TABLE anotacao (
    id UUID PRIMARY KEY,
    prontuario_id UUID NOT NULL REFERENCES prontuario (id),
    conteudo_enc TEXT NOT NULL,
    iv VARCHAR(64) NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_anotacao_prontuario ON anotacao (prontuario_id, criada_em);

CREATE TABLE auditoria_prontuario (
    id UUID PRIMARY KEY,
    prontuario_id UUID NOT NULL REFERENCES prontuario (id),
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    acao VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_prontuario ON auditoria_prontuario (prontuario_id);
