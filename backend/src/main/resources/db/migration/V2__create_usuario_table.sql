CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);
