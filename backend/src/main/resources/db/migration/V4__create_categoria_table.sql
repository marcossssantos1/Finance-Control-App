CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    cor VARCHAR(7),
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_categoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_categoria_tipo CHECK (tipo IN ('RECEITA', 'DESPESA'))
);

CREATE INDEX idx_categoria_usuario_id ON categoria (usuario_id);
