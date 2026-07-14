CREATE TABLE auditoria_anamnese (
    id UUID PRIMARY KEY,
    anamnese_id UUID NOT NULL REFERENCES anamnese (id),
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_anamnese_anamnese ON auditoria_anamnese (anamnese_id);

CREATE TABLE mensagem (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES paciente (id),
    psicologo_id UUID NOT NULL REFERENCES psicologo (id),
    remetente_id UUID NOT NULL REFERENCES usuario (id),
    conteudo TEXT NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT false,
    criada_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_mensagem_relacao ON mensagem (paciente_id, psicologo_id, criada_em);
