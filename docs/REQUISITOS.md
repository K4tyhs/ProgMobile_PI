# Rastreabilidade de requisitos — Programação Mobile (Entrega 2)

Cada requisito do documento do PI (PI_3ADS_202602_Proxima_Etapa_FINAL) aponta para onde está no código. Caminhos relativos a `app/src/main/java/br/org/proximaetapa/app/`.

## Entrega 2 — Programação Mobile (seção 10 do PDP)

| Pedido | Como foi atendido | Onde |
|---|---|---|
| Consumir dados de uma API REST do projeto | Retrofit com rotas definidas em contrato | `data/remote/ApiService.java`, `docs/API_CONTRATO.md` |
| Integrar autenticação | Login/cadastro com token JWT, sessão salva com criptografia, 401 = volta ao login | `ui/auth/`, `util/SessionManager.java`, `data/remote/ApiClient.java` |
| Armazenamento local | Room (SQLite) com 9 tabelas | `data/local/` |
| **Função principal:** sincronização dos dados do estudante | Envia a fila local e baixa tudo do aluno; a cada 15 min e quando a internet volta | `sync/SyncManager.java`, `sync/SyncWorker.java` |
| **Função principal:** presença por QR Code com persistência local e API | Lê o QR (ZXing) → valida → grava no Room → envia à API (ou fica na fila) | `ui/convite/ConviteActivity.java`, `domain/CheckinValidator.java`, `AppRepository.registrarPresenca` |
| Fluxo principal | Substituído pelo fluxo de navegação do grupo: Login → Home (avisos e cursos) → Bolsas → Calendário → Perfil; menu com Ingressos, Certificados e Testes | `ui/main/MainActivity.java` |
| Tratamento de JSON | Gson em todas as respostas, no banco de demonstração e nos testes de perfil | `data/remote/`, `data/repository/TesteRepository.java` |
| SQLite/Room | Entidades, DAOs, índices e transações | `data/local/` |
| Notificações | Novos cursos, mudança de agenda, certificados e avisos | `util/NotificationHelper.java`, `SyncManager.mesclarNotificacoes`, `SyncManager.avisarMudancasNaAgenda` |
| Histórico | Presenças registradas + participação por curso (presenças, faltas, %) | `ui/ingressos/MeusIngressosActivity.java` |
| Perda de conexão | Aviso na tela, fila offline e reenvio automático | `MainActivity` (aviso), `SyncWorker` (reenvio) |
| Card | Cartão digital com QR Code e nível da jornada | `ui/main/PerfilFragment.java` |
| Certificados | Lista, visualizar, baixar e compartilhar PDF | `ui/certificados/CertificadosActivity.java`, `util/CertificadoPdf.java` |
| Teste de perfil | DISC e RIASEC vindos da API, resultado salvo e sincronizado | `ui/testes/` |
| Mensagens/chat | Avisos na Home + chat ProxIA | `ui/main/HomeFragment.java`, `ui/chat/ChatActivity.java` |

Itens da Entrega 1 que continuam valendo: várias Activities, Intents (explícitas e implícitas), Fragments (4 abas), ConstraintLayout, TextView, ImageView, Button, RecyclerView e dado numérico (carga horária, encontros, % de presença).

**Layouts:** todas as telas usam apenas `ConstraintLayout` (cadeias, `Barrier`, `Guideline` e `Group`). Os campos são `EditText` simples com rótulo próprio, as listas usam `GridLayoutManager` com 1 coluna e as barras de progresso são feitas com `layout_constraintWidth_percent`.

## Requisitos funcionais (seção 4.1)

| ID | Requisito | Onde |
|---|---|---|
| RFM01 | Login, logout e sessão segura | `LoginActivity`, `CadastroActivity`, `SessionManager` (EncryptedSharedPreferences), logout no menu |
| RFM02 | Página inicial com resumo da jornada | `HomeFragment`: "Está chegando", avisos e cursos |
| RFM03 | Cursos disponíveis e inscritos, com descrição, período, carga horária e local | `HomeFragment` (filtro "Próximos cursos / Meus cursos"), `EventoDetalheActivity` |
| RFM04 | Aviso de novos cursos | Notificação do sistema na sincronização (`SyncManager.mesclarNotificacoes`) |
| RFM05 | Agenda individual | `CalendarioFragment` (semana; roxo = inscrito, verde = externo) + exportar para a agenda do celular |
| RFM06 | Presença por QR Code | `ConviteActivity` + `CheckinValidator` (alternativa: digitar o código) |
| RFM07 | Histórico de presença e participação | `MeusIngressosActivity` (participação por curso e histórico) |
| RFM08 | Card digital | `PerfilFragment` (cartão com QR Code) |
| RFM09 | Certificados: listar, visualizar, baixar | `CertificadosActivity` |
| RFM10 | Testes de perfil e resultado | `TestesActivity`, `QuestionarioActivity`, `ResultadoTesteActivity`, `domain/teste/TesteScorer` |
| RFM11 | Mensagens e comunicados | Avisos da Home (novos no topo, barra amarela = não lido, arrastar para apagar) |
| RFM12 | Chat de dúvidas | `ChatActivity` (ProxIA, com perguntas frequentes) |
| RFM13 | Notificações: novos cursos, **alterações de agenda**, certificados | `SyncManager` + `NotificationHelper` |
| RFM14 | Consultar e atualizar perfil | `PerfilFragment`, `EditarPerfilActivity` |
| RFM15 | Sincronização com tratamento de falhas | `SyncManager`, `SyncWorker` (WorkManager com rede obrigatória e nova tentativa) |

## Requisitos não funcionais (seção 5.1)

| Requisito | Como foi atendido |
|---|---|
| Plataforma | Android 7.0 (API 24) ou superior |
| Usabilidade | Telas do mockup, linguagem simples, confirmação antes de ações importantes |
| Acessibilidade | Contraste WCAG AA (paleta da Seção 14.1 da UX), rótulo visível em todo campo, `contentDescription` em ícones, áreas de toque ≥ 48dp, textos em `sp`, foco visível nos campos, alternativa ao QR Code, informação nunca só por cor |
| Desempenho | Índices no Room (data do evento, aluno, data do aviso), DiffUtil nas listas, banco e rede fora da thread principal |
| Persistência local | Room: tudo do aluno fica no celular e as telas leem do banco |
| Sincronização | Fila `sincronizado = false` + WorkManager quando a internet volta |
| Segurança | Token em armazenamento criptografado, senha nunca salva no app nem gravada no log, HTTPS obrigatório (HTTP só para `10.0.2.2`/localhost em desenvolvimento), `allowBackup=false`, 401 encerra a sessão |
| Privacidade | API devolve só os dados do aluno do token; logout e exclusão de conta apagam o banco local |
| Manutenibilidade | Camadas `ui` / `data` / `domain` / `sync` / `util`; regras de negócio em Java puro |
| Tratamento de erros | Mensagens claras (`ApiClient.mensagemDeErro`, `CheckinValidator.mensagem`) |

## Regras de negócio (seção 3)

| Regra | Onde |
|---|---|
| Cada aluno vê só os próprios dados | Token em toda chamada; o banco de demonstração filtra por aluno |
| Presença única por aluno, evento e data | Índice único no Room + `CheckinValidator` + checagem no servidor |
| Certificado só com critério cumprido (75% de presença) | `ParticipacaoCalculator`, `MockBackend.emitirCertificados` |
| Testes com perguntas, alternativas e critérios do banco | `GET /testes` → `TesteRepository` |
| Resultado do teste não é diagnóstico | Aviso nas telas de testes e de resultado |
| Dados fictícios em testes e apresentações | `assets/mock_db.json` usa a persona Ketlin (fictícia) |
| LGPD: coleta mínima | Endereço, cidade e escola são opcionais; o cartão não mostra dados demográficos |

## Testes unitários (`app/src/test`)

13 testes JUnit das regras de negócio: check-in (4), participação (3), badges (2), pontuação dos testes (2) e resumo de presenças e faltas (2).
