CREATE TABLE disponibilidade_plantao (
    id UUID PRIMARY KEY,
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    dia_semana VARCHAR(10),
    data_especifica DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_plantao_dia_ou_data CHECK (dia_semana IS NOT NULL OR data_especifica IS NOT NULL)
);

CREATE INDEX idx_plantao_psicologo ON disponibilidade_plantao (psicologo_id);
