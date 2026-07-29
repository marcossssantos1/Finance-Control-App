# Finance App — Controle Financeiro Pessoal

Aplicação para controle de finanças pessoais: contas, categorias, transações,
metas de orçamento e dashboard. Backend em Java/Spring Boot, frontend em React
(a entrar na Onda 3), autenticação via JWT.

## Status do projeto

🚧 Em desenvolvimento — Onda 1 (fundação): setup, autenticação, entidade de usuário.

## Stack

- **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA
- **Banco de dados:** PostgreSQL, Flyway (migrations)
- **Autenticação:** JWT (jjwt)
- **Frontend:** React (planejado — Onda 3)
- **Infra local:** Docker Compose
- **Deploy planejado:** Railway/Render (backend + DB), Vercel/Netlify (frontend)

## Estrutura do backend

```
backend/
 └── src/main/java/com/financeapp/
      ├── domain/       # entidades JPA
      ├── repository/   # interfaces Spring Data
      ├── service/       # regras de negócio
      ├── controller/    # endpoints REST
      ├── dto/           # objetos de request/response
      ├── config/        # configurações (security, etc.)
      └── exception/     # tratamento de erros
```

## Como rodar localmente

### Pré-requisitos
- Docker e Docker Compose instalados

### Passos

1. Copie o arquivo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```

2. Suba os containers:
   ```bash
   docker compose up --build
   ```

3. Verifique se a API está no ar:
   ```bash
   curl http://localhost:8080/api/ping
   curl http://localhost:8080/actuator/health
   ```

   Ambos devem responder com status `200` / `"status":"ok"` / `"status":"UP"`.

### Rodando sem Docker (Maven local)

Se preferir rodar o backend fora do container (com um Postgres local já no ar):

```bash
cd backend
mvn spring-boot:run
```

Certifique-se de que as variáveis `DB_URL`, `DB_USER`, `DB_PASS` e `JWT_SECRET`
estejam configuradas (via `.env` carregado no seu shell ou direto na IDE).

## Migrations

As migrations ficam em `backend/src/main/resources/db/migration`, gerenciadas
pelo Flyway. A cada nova entidade (Usuário, Conta, Transação...), uma nova
migration `V{n}__descricao.sql` é adicionada.

## Autenticação

- `POST /auth/register` — cria usuário (nome, email, senha)
- `POST /auth/login` — retorna JWT (`{ "token": "...", "tipo": "Bearer" }`)
- Rotas protegidas exigem header `Authorization: Bearer <token>`
- Rotas públicas: `/auth/register`, `/auth/login`, `/actuator/health`, `/api/ping`

## Contas

- `POST /contas` — cria conta vinculada ao usuário autenticado
- `GET /contas` — lista contas do usuário logado
- `GET /contas/{id}` — busca conta por id (404 se não existir ou for de outro usuário)
- Tipos disponíveis: `CORRENTE`, `POUPANCA`, `CARTEIRA`, `INVESTIMENTO`

## Categorias

- `POST /categorias` — cria categoria customizada vinculada ao usuário autenticado
- `GET /categorias` — lista categorias padrão do sistema + customizadas do usuário
- Tipos: `RECEITA`, `DESPESA`

## Transações

- `POST /transacoes` — cria transação (valida que conta e categoria pertencem/estão disponíveis ao usuário; tipo deve bater com o tipo da categoria)
- `GET /transacoes` — lista paginada, com filtros opcionais: `dataInicio`, `dataFim`, `contaId`, `categoriaId`, `page`, `size`
- `DELETE /transacoes/{id}` — remove transação (204)

## Roadmap (ondas)

- [x] **Onda 1** — Setup, autenticação JWT, entidade Usuário ✅ completa
- [x] **Onda 2** — Contas, Categorias, Transações ✅ completa
- [ ] **Onda 3** — Dashboard, gráficos, frontend React
- [ ] **Onda 4** — Metas de orçamento, deploy

## Licença

Projeto pessoal para fins de estudo e portfólio.
