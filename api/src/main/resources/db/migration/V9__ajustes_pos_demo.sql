-- Sprint 4.5 — Ajustes pós-demo (ver atas/2026-07-07-alinhamento-sprint-4.md)

ALTER TABLE paciente
    ADD COLUMN foto_url VARCHAR(500),
    ADD COLUMN idade INT;

CREATE TABLE psicologo_area_atuacao (
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    area VARCHAR(100) NOT NULL
);

CREATE INDEX idx_psicologo_area_atuacao ON psicologo_area_atuacao (area);

ALTER TABLE sessao
    ADD COLUMN tipo_atendimento VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL';

CREATE TABLE anamnese (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL UNIQUE REFERENCES paciente (id),
    conteudo_enc TEXT NOT NULL,
    iv VARCHAR(64) NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizada_em TIMESTAMP NOT NULL DEFAULT now()
);
