CREATE TABLE conta (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_conta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_conta_tipo CHECK (tipo IN ('CORRENTE', 'POUPANCA', 'CARTEIRA', 'INVESTIMENTO'))
);

CREATE INDEX idx_conta_usuario_id ON conta (usuario_id);
