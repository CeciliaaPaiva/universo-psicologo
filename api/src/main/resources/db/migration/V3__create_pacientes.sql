CREATE TABLE paciente (
    id UUID PRIMARY KEY REFERENCES usuario (id),
    faixa_renda VARCHAR(30) NOT NULL
);
