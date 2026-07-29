CREATE TABLE transacao (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    conta_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(15, 2) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    data_transacao DATE NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_transacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_transacao_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT chk_transacao_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT chk_transacao_valor_positivo CHECK (valor > 0)
);

CREATE INDEX idx_transacao_usuario_id ON transacao (usuario_id);
CREATE INDEX idx_transacao_conta_id ON transacao (conta_id);
CREATE INDEX idx_transacao_categoria_id ON transacao (categoria_id);
CREATE INDEX idx_transacao_data ON transacao (data_transacao);
