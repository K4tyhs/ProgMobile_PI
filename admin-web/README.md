# Próxima Etapa — Painel administrativo (admin-web)

Painel web da equipe da ONG **Próxima Etapa**, que ajuda jovens de escola pública da periferia de São Paulo a
chegar ao ensino superior. Projeto Interdisciplinar do 3º semestre de ADS da FECAP (Grupo 1, turma 3NAADS).

A solução tem três partes: o **app Android** (alunos), a **API** Node + Express + TypeScript + Prisma e este
**painel web** (equipe da ONG). Alunos nunca usam o painel. A especificação completa — regras, modelo de dados,
endpoints, telas e plano por fases — está no documento **"Módulo Web Administrativo — Documento de Implementação"**.

## O que o painel faz

| Área | O que a equipe faz | Tela |
|---|---|---|
| **Dashboard** | Palestra do dia com atalho para o check-in; indicadores do mês (com filtro de mês); palestras por mês; perfis DISC e RIASEC; ranking do mês (top 10) e geral (top 5) | 9.2 |
| **Alunos** | Busca por nome/e-mail, filtros por status e DISC; detalhe com inscrições, presenças, faltas (justificar), certificados e testes | 9.3, 9.4 |
| **Palestras** | Lista com filtros; cadastro/edição com evento novo ou existente; detalhe com as abas **Inscritos** (e lista de espera), **Presença** (presença manual, encerrar lista, justificar falta), **Grupos DISC** (soft skills) e **Certificados** (liberar em lote) | 9.5 a 9.7 |
| **Check-in** | Pelo celular: escolhe a palestra do dia, lê o QR Code do convite pela câmera e mostra o resultado em cores | 9.8 |
| **Testes de perfil** | Editor das perguntas e alternativas do DISC e do RIASEC | 9.9 |
| **Comunicação** | Mensagens segmentadas (todos, inscritos de uma palestra, letra DISC) com confirmação do nº de destinatários; caixa de dúvidas com resposta e fechamento | 9.10 |

## Como rodar

Requisitos: Node 20+ e npm.

```bash
cp .env.example .env
npm install
npm run dev       # http://localhost:5173
```

Usuário de teste: `admin@proximaetapa.test` / `admin123`.

Outros comandos:

```bash
npm test          # testes unitários e de integração (Vitest)
npx oxlint src    # lint
npm run build     # versão de produção em dist/ (inclui checagem de tipos)
npm run preview   # serve o dist/ localmente
```

A câmera do check-in só funciona em **HTTPS** (ou em `localhost`). Para testar no celular durante o
desenvolvimento, use um túnel HTTPS (ex.: `npx localtunnel --port 5173`) ou o deploy.

## Sem API por enquanto: o mock

Enquanto as rotas `/admin/*` da API não existem, o painel roda sobre um **mock** que imita essas rotas com o mesmo
formato de dados e as mesmas validações:

- `src/mocks/mockDataProvider.ts` — mapeamento recurso do Refine → rota da API (comentado no topo do arquivo).
- `src/mocks/servicos.ts` — a "camada de serviço" do mock, que aplica as regras de `src/regras/`.
- `src/mocks/seed.ts` — dados **fictícios** (LGPD: nenhum nome ou e-mail real), com datas relativas a hoje.

O que é feito no painel fica salvo no navegador. Para voltar aos dados originais, limpe o armazenamento do site.

### Ligando a API

1. Em `src/providers/dataProvider.ts`, troque o mock por um provider que chama `${VITE_API_URL}/admin/...` com o
   JWT da sessão, seguindo o mapeamento do mock, e mude `USANDO_MOCK` para `false`.
2. Em `src/providers/authProvider.ts`, troque o corpo de `login` pela chamada a `POST /admin/auth/login`.
3. `src/types/dominio.ts` é o **contrato**: o formato que cada rota deve devolver.
4. `src/regras/` tem as regras de negócio puras e testadas (faltas e bloqueio, certificados, ranking, grupos DISC,
   comunicação, testes de perfil). A API deve seguir as mesmas regras.

## Testes

| Tipo | Arquivos | O que cobre |
|---|---|---|
| Unitários | `src/regras/*.test.ts`, `src/pages/palestras/*.test.ts` | Regras puras: 72h, pontos e bloqueio, justificativa, certificado único por evento, ranking, grupos DISC, segmentos de mensagem, validação dos testes de perfil |
| Serviço | `src/mocks/servicos.test.ts` | Fluxos sobre os dados fictícios (o roteiro dos testes da API) |
| Integração | `src/mocks/integracao.test.ts` | Pela camada de rotas que as telas usam: **check-in por QR** (gravada, já registrado, QR inválido, não inscrito) e **encerramento de lista** (409 antes do início, faltas geradas, check-in fechado, justificativa, certificados) |

## Deploy

O painel é um site estático (SPA): `npm run build` gera `dist/`. Há duas opções prontas:

**Vercel** (recomendada) — `vercel.json` já redireciona as rotas para o `index.html` e define cabeçalhos de
segurança, incluindo `Permissions-Policy: camera=(self)` para o check-in. A Vercel serve em HTTPS automaticamente.
Configure as variáveis de ambiente do `.env.example` no painel do projeto.

**Container Nginx** — `Dockerfile` (roda os testes, gera o build e serve com `nginx.conf`):

```bash
docker build --build-arg VITE_API_URL=https://api.exemplo.org -t proxima-etapa-admin .
docker run -p 8080:80 proxima-etapa-admin
```

O HTTPS fica no proxy ou balanceador na frente do container.

Variáveis (`.env.example`): `VITE_API_URL` (URL da API), `VITE_ROUTER` (`browser` em produção) e
`VITE_SHOW_TEST_LOGIN`. Tudo que começa com `VITE_` vai para o navegador: **nunca** coloque segredos ali.

## Estrutura

```
src/
  config/resources.ts   as 6 áreas do menu
  types/dominio.ts      contrato dos dados com a API
  regras/               regras de negócio puras + testes
  mocks/                dados fictícios e mock das rotas /admin/*
  providers/            authProvider (login), dataProvider (API), sessão
  hooks/useAcaoAdmin.ts ações que não são CRUD + atualização das listas
  lib/datas.ts          datas no formato brasileiro
  components/ui/        Button, Card, Badge, DataTable, Tabs, Alert, ConfirmDialog, BarChart, form/Field…
  components/layout/    AppLayout, Sidebar, Topbar
  pages/                uma pasta por área (dashboard, alunos, palestras, checkin, testes, comunicacao)
  styles/               tokens.css (cores, fontes, espaçamento) e global.css
```

## Convenções do time

- Do Figma do app vem só o **visual** (cores, formas, componentes); funcionalidades e textos seguem o documento.
- Cor, raio, sombra, fonte ou espaçamento novo entra em `src/styles/tokens.css`, nunca solto num componente.
- Regra de negócio fica em `src/regras/` (pura e testada); a tela só exibe.
- Coleta mínima (LGPD): o painel mostra do aluno só nome, e-mail, situação de faltas e resultados dos testes. Sem CPF.
- Acessibilidade: rótulo visível em todo campo, foco visível, `aria-live` nos avisos, confirmação com `ConfirmDialog`.

## Stack

React 19 · Vite · TypeScript · [Refine](https://refine.dev) v5 (headless) · React Router 7 · CSS Modules ·
lucide-react · `@zxing/browser` (leitura de QR) · Vitest · oxlint.
