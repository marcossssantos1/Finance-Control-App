# Deploy no Render

Este projeto já tem um `render.yaml` (Render Blueprint) na raiz, que descreve
automaticamente o backend + banco de dados. Você só precisa conectar o repo.

## Passo a passo

1. **Crie uma conta** em [render.com](https://render.com) (dá pra logar com GitHub direto)

2. No dashboard, clique em **New +** → **Blueprint**

3. Conecte seu repositório `Finance-Control-App` (autorize o Render a acessar
   seus repos do GitHub, se for a primeira vez)

4. O Render vai detectar o `render.yaml` automaticamente e mostrar um preview:
   - 1 Web Service (`finance-app-backend`) — free tier
   - 1 PostgreSQL Database (`finance-app-db`) — free tier

5. Clique em **Apply** — o Render vai:
   - Criar o banco Postgres primeiro
   - Buildar a imagem Docker do backend (usando `backend/Dockerfile`)
   - Conectar as variáveis de ambiente automaticamente (host, porta, nome do
     banco, usuário, senha — tudo isso já mapeado no `render.yaml`)
   - Gerar um `JWT_SECRET` aleatório e seguro sozinho (`generateValue: true`)

6. O primeiro deploy demora alguns minutos (build da imagem + aplicação das
   migrations Flyway). Acompanhe o log em tempo real no dashboard.

7. Quando finalizar, o Render te dá uma URL pública, algo como:
   ```
   https://finance-app-backend.onrender.com
   ```

8. Teste:
   ```bash
   curl https://finance-app-backend.onrender.com/actuator/health
   curl https://finance-app-backend.onrender.com/api/ping
   ```

## Deploy automático (CD)

Depois desse primeiro setup, **todo push na branch `main` já dispara um novo
deploy automaticamente** — isso é comportamento padrão do Render ao conectar
via Blueprint/GitHub, não precisa configurar nada extra no GitHub Actions.

Fluxo completo fica assim:
```
push na main → GitHub Actions (CI: build + testes) → Render detecta o push → novo deploy
```

O CI e o CD rodam em paralelo (não um depende do outro neste setup simples).
Se quiser que o deploy só aconteça depois do CI passar, dá pra configurar isso
depois usando "Deploy Hooks" do Render disparados via GitHub Actions — mas
para o estágio atual do projeto, o paralelo já é suficiente.

## Limitações do free tier (importante saber)

- **Cold start**: o serviço "dorme" depois de ~15 min sem receber requisições.
  A primeira requisição depois disso demora alguns segundos a mais (ele
  "acorda"). Normal, não é bug.
- **Banco expira em 90 dias**: o Postgres free do Render é apagado depois de
  90 dias. Quando estiver perto disso, o Render avisa por email — aí é só
  recriar o banco (ou nesse ponto considerar migrar para um plano pago, ou
  outro provedor com Postgres persistente gratuito).
- **512 MB de RAM** no plano free — suficiente para esse projeto, mas se
  crescer bastante pode precisar de upgrade.

## Variáveis de ambiente (referência)

Todas já configuradas automaticamente pelo `render.yaml`, mas caso precise
ajustar manualmente no dashboard (Settings → Environment):

| Variável | Origem |
|---|---|
| `SPRING_PROFILES_ACTIVE` | fixo: `prod` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` | do banco Render, automático |
| `JWT_SECRET` | gerado automaticamente pelo Render |
| `JWT_EXPIRATION_MS` | fixo: `86400000` (24h) |
