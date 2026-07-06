CREATE TABLE slot (
    id UUID PRIMARY KEY,
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    google_event_id VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_slot_psicologo_inicio ON slot (psicologo_id, inicio);
