CREATE TABLE psicologo (
    id UUID PRIMARY KEY REFERENCES usuario (id),
    crp VARCHAR(20) NOT NULL,
    especializacao VARCHAR(255),
    curriculo_url VARCHAR(500),
    politica_cancelamento TEXT,
    status_aprovacao VARCHAR(30) NOT NULL DEFAULT 'PENDENTE_APROVACAO',
    motivo_reprovacao TEXT
);

CREATE INDEX idx_psicologo_status_aprovacao ON psicologo (status_aprovacao);
