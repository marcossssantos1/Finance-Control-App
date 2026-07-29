-- Migration inicial do projeto.
-- As tabelas de domínio (usuario, conta, transacao, etc.) serão adicionadas
-- nas próximas migrations, conforme as ondas seguintes (Onda 1 - Ticket B em diante).

-- Extensão usada para gerar UUIDs, caso opte por UUID como PK no futuro.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
