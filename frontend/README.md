# Finance App — Frontend

Frontend em React + TypeScript (Vite) para o Finance App, consumindo a API
Java/Spring Boot em `../backend`.

## Stack

- Vite + React 18 + TypeScript
- react-router-dom v6
- axios
- Tailwind CSS
- react-hook-form + zod

## Como rodar localmente

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

A aplicação sobe em `http://localhost:5173`.

Certifique-se de que o backend está rodando (via Docker Compose, na raiz do
repositório) em `http://localhost:8080`, que é o valor padrão de
`VITE_API_URL` no `.env.example`.

## Apontando para a API em produção

Edite o arquivo `.env` (não o `.env.example`) e troque `VITE_API_URL` pela
URL do backend publicado no Render, por exemplo:

```
VITE_API_URL=https://finance-app-backend.onrender.com
```

Depois rode `npm run dev` (ou `npm run build` para gerar o bundle de
produção).

## Autenticação

- Registro: `/registro`
- Login: `/login`
- Rotas protegidas (ex.: `/dashboard`) exigem estar autenticado; caso
  contrário o usuário é redirecionado para `/login`.
- O token JWT é salvo em `localStorage` (`finance_app_token`) e enviado
  automaticamente em toda requisição autenticada via interceptor do axios.
- Se a API retornar `401`, o token é limpo e o usuário é redirecionado para
  `/login`.

## Estrutura

```
src/
├── api/          # client axios + funções de autenticação
├── contexts/      # AuthContext (estado de autenticação)
├── routes/        # ProtectedRoute
├── pages/         # Login, Registro, DashboardPlaceholder
├── types/         # tipos compartilhados (auth, erro da API)
├── App.tsx        # rotas
└── main.tsx
```
