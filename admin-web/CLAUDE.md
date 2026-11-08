# CLAUDE.md — Painel administrativo Próxima Etapa (admin-web)

Contexto para o Claude Code continuar este projeto. Leia inteiro antes de mexer no código.

## O projeto

- Projeto Interdisciplinar do 3º semestre de ADS da FECAP (Grupo 1, turma 3NAADS), feito para a ONG
  **Próxima Etapa**, que ajuda jovens de escola pública da periferia de São Paulo a chegar ao ensino superior.
- A solução tem 3 partes: **app Android** (alunos), **API Node + Express + TypeScript + Prisma** (back, da Katiely; o painel web
  não usa a API: fala direto com o Supabase)
  e este **painel web administrativo** (equipe da ONG). Alunos nunca usam o painel.
- Quem conduz o painel: Laura (UX/UI e Testes e Qualidade). Implementação e rotas `/admin/*` na API: Katiely.
- Especificação completa: documento "Módulo Web Administrativo — Documento de Implementação"
  (https://claude.ai/artifact/XKCUYJNwQYXYHyx4HWyhjp). As seções citadas abaixo (4.1, 9.7…) são dele.

## Como trabalhar com a Laura

- **Não desenvolva nada sem a confirmação dela.** Proponha o plano, espere o "pode seguir".
- Português do Brasil em tudo: código de domínio, comentários, textos da tela e respostas.
- Respostas curtas e objetivas, em listas quando ajudar. Ela prefere ver o painel rodando a receber prints.
- Do Figma do app (mobile) vem **só o visual** (cores, formas, componentes). Funcionalidades e textos vêm do
  documento de implementação. Nunca copiar conteúdo das telas do app.
- Quando uma decisão de regra não estiver clara, pergunte com uma sugestão, em vez de inventar.

## Stack

- React 19 + Vite + TypeScript, **Refine v5** (`@refinedev/core`, headless) + `@refinedev/react-router` (React Router 7).
- CSS Modules + tokens em `src/styles/tokens.css`. Sem Tailwind. Ícones: `lucide-react`.
- Fontes: Inter (texto, como no app) e Baloo 2 (títulos), via `@fontsource`.
- Leitura de QR: `@zxing/browser` (tela de check-in carregada sob demanda).
- Testes: Vitest (`npm test`). Lint: oxlint (`npx oxlint src`).

```bash
npm install
npm run dev     # http://localhost:5173 — login: conta Supabase com papel admin
npm test
npm run build
```

## Estrutura

```
src/
  config/resources.ts   as 5 áreas do menu (rota, ícone, descrição)
  types/dominio.ts      CONTRATO dos dados com a API (formato de cada rota /admin/*)
  regras/presenca.ts    regras de faltas, pontos e bloqueio (seção 4.1) — puras e testadas
  regras/certificado.ts quem recebe certificado, um por evento (seção 4.2) — puras e testadas
  regras/ranking.ts     ranking de melhores alunos (seção 4.4) — pura e testada
  regras/comunicacao.ts destinatários por segmento, validação da mensagem, status da dúvida (9.10) — puras e testadas
  regras/gruposDisc.ts  formação dos grupos DISC (seção 4.3) — pura e testada
  regras/testesPerfil.ts validação do editor de testes DISC/RIASEC (9.9) — pura e testada
  mocks/                dados fictícios + mock das rotas /admin/* (seed, mockDb, servicos, provider)
  lib/supabase.ts       cliente Supabase (só chave publishable) + checagem do .env
  providers/            authProvider (Supabase Auth + has_role admin), dataProvider → providers/supabase/
  hooks/useAcaoAdmin.ts ações que não são CRUD (check-in, encerrar, justificar) + invalidação
  lib/datas.ts          datas no formato brasileiro, situação da palestra
  components/ui/        Button, Card, Badge, DateTile, DataTable, Tabs, Alert, ConfirmDialog, ProgressBar, BarChart, form/Field…
  components/layout/    AppLayout, Sidebar, Topbar
  pages/                login, dashboard, alunos, palestras (lista, form, detalhe, abas), checkin, testes, comunicacao
```

## Convenções

- Cor, raio, sombra, fonte ou espaçamento novo entra em `tokens.css`, nunca solto num componente.
- Componentes visuais reutilizáveis ficam em `components/ui`; não estilizar do zero o que já existe.
- Regra de negócio fica fora da tela: em `src/regras/` (pura, com teste) e no mock/API. A tela só exibe.
- Todo `localStorage` dentro de try/catch (pode não existir).
- Dados sempre fictícios (LGPD, seção 5.2 do briefing). Nada de nome/e-mail real em seed, teste ou print.
- Coleta mínima: o painel mostra do aluno só nome, e-mail e a situação de faltas. Não exibir CPF.
- Acessibilidade: rótulo visível em todo campo, foco visível, `aria-live` em feedback, respeitar reduced-motion.
- Sem `window.confirm/alert`: usar `ConfirmDialog`.

## Banco: Supabase (dados reais) — o mock só serve aos testes

- O painel fala direto com o Supabase (sem a API Node). `providers/supabase/supabaseDataProvider.ts` implementa os
  MESMOS recursos/rotas custom do mock, convertendo as tabelas do app (`mapeamento.ts`, testado).
- Modelo do banco: cada linha de `eventos` é uma edição (palestra.id === eventoId). `tipo=EXTERNO` não é palestra.
  `requer_testes` = soft skills. QR = `PE:INGRESSO:<inscricoes.id>:<codigo_ingresso>`;
  o check-in chama a função `validar_ingresso` do banco (janela de horário, origem LEITOR, certificado automático). Pontos/bloqueio são calculados de `faltas`/`bloqueios`.
- `supabase/painel_admin.sql` (rodar no SQL Editor) acrescenta vagas, lista_encerrada, faltas, bloqueios e RLS de admin.
  Só ACRESCENTAR no banco: o app Android usa as mesmas tabelas.
- Sem tabela ainda: dúvidas, grupos DISC, lista de espera, badges (listas vazias; ações respondem 501).

## Mock (só testes)

- `src/mocks/mockDataProvider.ts` imita as rotas `/admin/*` (mapeamento no topo do arquivo). Recursos:
  `alunos`, `mensagens`, `duvidas` (lista e detalhe), `testes` (getOne: DISC | RIASEC), `grupos` (filtro `palestraId`), `eventos`, `palestras`, `inscricoes`, `presencas` e `certificados` (filtro `palestraId`),
  mais `custom` para `GET /admin/dashboard?mes=AAAA-MM`, `GET /admin/ranking?mes=AAAA-MM`,
  `GET /admin/mensagens/destinatarios`, `POST /admin/mensagens`, `POST /admin/duvidas/:id/respostas`,
  `PATCH /admin/duvidas/:id/fechar`, `POST /admin/palestras/:id/grupos`, `PUT /admin/grupos/:id/membros`,
  `PUT /admin/testes/:tipo`, `POST /admin/palestras/:id/checkin`, `POST /admin/palestras/:id/presencas`,
  `POST /admin/palestras/:id/encerrar`, `POST /admin/palestras/:id/certificados` e `PATCH /admin/faltas/:id/justificar`.
- `src/mocks/servicos.ts` é a "camada de serviço" do mock e usa os arquivos de `src/regras/`.
- Os dados ficam salvos no navegador (chave `proxima-etapa:mock-db:v6`). Mudou o formato do banco
  fictício? Suba a `VERSAO` em `mockDb.ts`.
- Token de QR de teste (só nos testes do mock): `src/mocks/qrToken.ts`. O QR real do app é `PE:INGRESSO:<inscrição>:<código>`.

## Regras de negócio fechadas (não mudar sem a Laura)

**Faltas e bloqueio (4.1)** — implementado em `regras/presenca.ts`
- Gera 1 ponto: inscrito confirmado sem presença, ou quem cancelou com **menos de 72h** do início.
- **2 pontos → bloqueio de 30 dias** para novas inscrições; os pontos zeram quando o bloqueio é aplicado.
- Inscrições feitas antes do bloqueio continuam válidas (o aluno bloqueado ainda faz check-in nelas).
- Admin pode justificar falta: tira aquele ponto. Se a falta gerou um bloqueio em vigor, o bloqueio é
  cancelado e a outra falta volta a valer 1 ponto.
- Faltas são geradas quando o admin clica "Encerrar lista" (só depois do início da palestra; não reabre).
- O aluno é avisado no app a cada ponto e no bloqueio (responsabilidade da API/app).

**Vagas e lista de espera (4.5)**
- Toda palestra tem limite de vagas. Lotou → entra na lista de espera com posição.
- Quando um confirmado cancela, o 1º da fila recebe convite e tem **24h para aceitar** (nunca depois do
  início da palestra). Recusou ou expirou → passa para o próximo. Quem aceita e falta leva 1 ponto normal.
- Bloqueado não entra na lista; em soft skills o teste DISC é obrigatório para entrar.

**Certificado (4.2)** — implementado em `regras/certificado.ts`. Todo evento dura 1 dia; presença (QR ou manual)
= certificado, **um por evento** (reedição não gera outro). Pode liberar assim que a presença é registrada, sem
esperar "Encerrar lista". O painel **só libera**: um botão "Liberar certificados" (em lote, para todos os presentes
pendentes) avisa a API. A API gera o PDF na hora (sem armazenar arquivo; campos: nome, palestra, data, carga
horária, código de validação) e o app notifica o aluno. O painel não gera, não guarda e não abre PDF.
Web e app são módulos separados, ligados só pela API.

**Grupos DISC (4.3)** — implementado em `regras/gruposDisc.ts`; só em palestras de soft skills. Grupos de 4 (um D, I, S, C); nº de grupos = total ÷ 4;
completa evitando letra repetida; quem sobra vai para grupos existentes (ficam com 5). Gerados na véspera,
admin pode regerar/ajustar. O convite do aluno mostra só o número do grupo.
Participam os inscritos confirmados. Com menos de 4, forma 1 grupo só. Pela regra do ÷4 arredondado para baixo,
5 a 7 confirmados também viram 1 grupo (com 5 a 7 pessoas). Ajuste manual = mover a pessoa para outro grupo;
nenhum grupo pode ficar vazio. Lista encerrada: grupos não mudam mais.

**Editor de testes (9.9)** — `regras/testesPerfil.ts`. Toda pergunta com texto e ≥ 2 alternativas; toda
alternativa com texto e a letra/área que pontua; toda letra/área precisa ser pontuada por alguma alternativa (senão
o resultado fica impossível). Salvar vale para as próximas respostas; resultados já salvos não mudam.

**Ranking (4.4)** — implementado em `regras/ranking.ts`. Nº de palestras frequentadas (reedição conta de novo);
desempate por nº de badges (contados no app; o painel só exibe o número). Do mês (top 10) e geral (top 5); quem
não frequentou nenhuma palestra no período não entra. Faltas não descontam.

**Outros** — Evento = conteúdo fixo (título, descrição, carga horária, tipo); Palestra = cada edição (data,
horário, sala opcional, vagas). Papéis ADMIN e STAFF existem no modelo, mas a 1ª versão tem só ADMIN (seed).
QR: o app gera um token curto e assinado; o painel lê pela câmera e manda para a API. Câmera exige HTTPS
(ou localhost).

**Comunicação (9.10)** — implementado em `regras/comunicacao.ts`. Mensagens segmentadas viram notificação no app:
TODOS = todos os alunos cadastrados (bloqueados também); PALESTRA = confirmados + lista de espera/convidados
(cancelados não); DISC = alunos com a letra (quem não fez o teste fica de fora). Antes de enviar, confirmação com o
nº de destinatários; sem destinatário a API recusa. Histórico guarda o nº que valia no envio; mensagem enviada não
é apagada. Dúvidas: resposta do admin → RESPONDIDA; nova mensagem do aluno (app) → ABERTA; fechada não aceita
resposta (o aluno abre outra). Telas: abas Mensagens / Dúvidas abertas / Respondidas / Fechadas (o documento cita só
Abertas e Respondidas; Fechadas entrou para as fechadas não sumirem).

## Onde estamos (plano da seção 10)

- [x] **Fase 0** — base, tokens, layout, login fake.
- [x] **Fase 1** — palestras: lista (9.5), cadastro/edição com evento novo ou existente (9.6), detalhe com aba
  Inscritos e lista de espera (9.7).
- [x] **Fase 2** — check-in por QR (9.8), aba Presença, encerrar lista, justificar falta.
- [x] Polimento visual + dashboard com palestra de hoje, indicadores do mês e próximas palestras (parte da 9.2).
- [x] **Fase 3 — Certificados:** aba Certificados da palestra (presentes com situação Pendente / Liberado /
  Já recebeu deste evento) e botão "Liberar certificados"; rotas `GET` e `POST /admin/palestras/:id/certificados`
  no mock (seed: pal-5 já liberada, pal-4 pendente).
- [x] **Fase 4** — Alunos: lista com busca e filtros de status e DISC (9.3); detalhe com abas Inscrições,
  Presenças, Faltas (com Justificar), Certificados (só a data de liberação, sem PDF) e Testes (9.4).
  Dashboard: filtro de mês, palestras por mês (6 meses), distribuição DISC e RIASEC, rankings do mês e geral.
  "Ativos na semana" segue "—" até o app gravar `AcessoDiario`. Inscritos por próxima palestra já aparecem nas
  barras de "Próximas palestras". Favicon = ícone oficial (`public/favicon.png`).
- [x] **Fase 5** — Comunicação em `/comunicacao` (abas, `?aba=`) e conversa em `/comunicacao/duvidas/:id`.
- [x] **Fase 6** — Editor de testes em `/testes/disc` e `/testes/riasec` (9.9) e aba Grupos DISC no detalhe da
  palestra de soft skills (gerar/regerar, mover pessoa de grupo). A geração automática na véspera é da API (cron).
- [x] **Fechamento (parte do painel)** — README final; testes de integração pela camada de rotas
  (`src/mocks/integracao.test.ts`: check-in por QR e encerramento de lista); deploy preparado para Vercel
  (`vercel.json`: rotas SPA + cabeçalhos, `camera=(self)`). Docker/Nginx removidos: o painel é estático + Supabase.
- [x] **Supabase** — painel ligado direto ao banco do app (login Supabase Auth com papel admin).
- [ ] **Falta:** publicar em HTTPS; tabelas de dúvidas, grupos DISC, lista de espera e badges; o app passar a
  respeitar `vagas` e `bloqueios` na inscrição (função `inscrever`).

## Pendências e avisos

- O documento de implementação ainda diz, na seção 5, que o QR fica no "card digital". O app (Figma) mostra o QR
  dentro do **convite** de cada palestra. Para o painel não muda nada (ele só lê o token), mas o time precisa alinhar.
- O Figma do app tem textos de punição diferentes das regras acima ("suspenso 3 meses", "próximos 3 eventos") e
  mostra CPF no convite. As regras deste arquivo são as decididas; avisar a Laura se isso aparecer.
- No modelo da seção 7 do documento, `Falta` não tem `bloqueioId` e `Bloqueio` não tem `cancelado`. O código
  (`types/dominio.ts`) já tem os dois, porque a regra de justificativa precisa deles. Vale o código.
- Os testes em `src/regras/*.test.ts`, `src/mocks/servicos.test.ts` e `src/mocks/integracao.test.ts` servem de base para a entrega de
  Testes e Qualidade (4 unitários + 2 de integração) — não apagar.
