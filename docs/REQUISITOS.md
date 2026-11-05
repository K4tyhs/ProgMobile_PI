# Rastreabilidade de requisitos — Programação Mobile (Entrega 2)

Cada requisito do documento do PI (PI_3ADS_202602_Proxima_Etapa_FINAL) aponta para onde está no código. Caminhos relativos a `app/src/main/java/com/example/proximaetapa/`.

## Entrega 2 — Programação Mobile (seção 10 do PDP)

| Pedido | Como foi atendido | Onde |
|---|---|---|
| Consumir dados de uma API REST do projeto | Retrofit chamando o Supabase: Auth (`auth/v1/*`) e funções SQL por `POST rest/v1/rpc/<nome>` com `apikey` + `Authorization: Bearer` | `data/remote/ApiService.java`, `data/remote/ApiClient.java` |
| Integrar autenticação | Supabase Auth: login (`grant_type=password`) ou cadastro (`signup`, com nome, endereço, cidade e escola em `user_metadata`) → `rpc/meu_perfil`; só abre a Home se `meu_perfil` responder 200. Tokens criptografados e renovados com `grant_type=refresh_token`; 401 sem renovação = apaga os dados locais e volta ao login. Sem login falso: o modo demonstração só existiria com `BuildConfig.MODO_DEMO` (desligado) | `ui/auth/`, `util/SessionManager.java`, `data/remote/ApiClient.java`, `ui/SessaoGuard.java` |
| Armazenamento local | Room (SQLite) com 9 tabelas | `data/local/` |
| **Função principal:** sincronização dos dados do estudante | Envia a fila local (inscrições, cancelamentos, resultados de testes) e baixa tudo do aluno, incluindo as presenças confirmadas pelo servidor. Ao abrir o app, quando a internet volta, a cada 15 min e a cada 30 s com o ingresso aberto | `sync/SyncManager.java`, `sync/SyncWorker.java`, `ConviteActivity` |
| **Função principal:** presença por QR Code com persistência local e API | O app gera (ZXing, offline) um QR único por inscrição `PE:INGRESSO:{id}:{codigoIngresso}`. O leitor web da Próxima Etapa valida no Supabase (`validar_ingresso`). A presença volta pelo `rpc/minhas_presencas`, é gravada no Room e gera o aviso "Presença confirmada". O app não lê QR nem registra presença | `ui/convite/ConviteActivity.java`, `domain/IngressoQr.java`, `domain/CheckinValidator.java` (informativo), `SyncManager.atualizarPresencas` |
| Fluxo principal | Substituído pelo fluxo de navegação do grupo: Login → Home (avisos e cursos) → Bolsas → Calendário → Perfil; menu com Ingressos, Certificados e Testes | `ui/main/MainActivity.java` |
| Tratamento de JSON | Gson em todas as respostas, no banco de demonstração e nos testes de perfil | `data/remote/`, `data/repository/TesteRepository.java` |
| SQLite/Room | Entidades, DAOs, índices e transações | `data/local/` |
| Notificações | Novos cursos, mudança de agenda, certificados e avisos | `util/NotificationHelper.java`, `SyncManager.mesclarNotificacoes`, `SyncManager.avisarMudancasNaAgenda` |
| Histórico | Presenças registradas + participação por curso (presenças, faltas, %) | `ui/ingressos/MeusIngressosActivity.java` |
| Perda de conexão | Aviso na tela, fila offline e reenvio automático. O QR do ingresso funciona sem internet (gerado no celular) | `MainActivity` (aviso), `SyncWorker` (reenvio) |
| Card | Cartão digital com QR Code e nível da jornada | `ui/main/PerfilFragment.java` |
| Certificados | Lista, visualizar, baixar e compartilhar PDF | `ui/certificados/CertificadosActivity.java`, `util/CertificadoPdf.java` |
| Teste de perfil | DISC e RIASEC vindos da API, resultado salvo e sincronizado | `ui/testes/` |
| Mensagens/chat | Avisos na Home + chat ProxIA | `ui/main/HomeFragment.java`, `ui/chat/ChatActivity.java` |

Itens da Entrega 1 que continuam valendo: várias Activities, Intents (explícitas e implícitas), Fragments (4 abas), ConstraintLayout, TextView, ImageView, Button, RecyclerView e dado numérico (carga horária, encontros, % de presença).

**Layouts:** as telas usam `ConstraintLayout` (cadeias, `Barrier`, `Guideline` e `Group`), sem `LinearLayout`. As 4 telas de autenticação têm `ScrollView` como raiz (com um `ConstraintLayout` dentro). Os campos são `EditText` simples com rótulo próprio, as listas usam `GridLayoutManager` com 1 coluna e as barras de progresso são feitas com `layout_constraintWidth_percent`.

## Requisitos funcionais (seção 4.1)

| ID | Requisito | Onde |
|---|---|---|
| RFM01 | Login, logout e sessão segura | `LoginActivity`, `CadastroActivity`, `SessionManager` (EncryptedSharedPreferences), logout no menu |
| RFM02 | Página inicial com resumo da jornada | `HomeFragment`: "Está chegando", avisos e cursos |
| RFM03 | Cursos disponíveis e inscritos, com descrição, período, carga horária e local | `HomeFragment` (filtro "Próximos cursos / Meus cursos"), `EventoDetalheActivity` |
| RFM04 | Aviso de novos cursos | Notificação do sistema na sincronização (`SyncManager.mesclarNotificacoes`) |
| RFM05 | Agenda individual | `CalendarioFragment` (semana; roxo = inscrito, verde = externo) + exportar para a agenda do celular |
| RFM06 | Presença por QR Code | `ConviteActivity` mostra o QR único do ingresso (só para inscrição ATIVA e confirmada pelo servidor; cancelada ou externo = "Ingresso indisponível"; offline = "Sincronizando ingresso…"), com brilho máximo e a situação da janela de check-in (`CheckinValidator`). A validação é do leitor web |
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
| Acessibilidade | Contraste WCAG AA (paleta da Seção 14.1 da UX), rótulo visível em todo campo, `contentDescription` em ícones, áreas de toque ≥ 48dp, textos em `sp`, foco visível nos campos, código do ingresso escrito abaixo do QR, informação nunca só por cor |
| Desempenho | Índices no Room (data do evento, aluno, data do aviso), DiffUtil nas listas, banco e rede fora da thread principal |
| Persistência local | Room: tudo do aluno fica no celular e as telas leem do banco |
| Sincronização | Fila `sincronizado = false` (inscrições, cancelamentos, resultados de testes) + WorkManager quando a internet volta. Presenças não têm fila: vêm só do servidor |
| Segurança | Token em armazenamento criptografado, senha nunca salva no app nem gravada no log, só HTTPS, `allowBackup=false`, 401 sem renovação encerra a sessão, sem permissão de câmera, QR do ingresso sem dado pessoal |
| Privacidade | API devolve só os dados do aluno do token; logout e exclusão de conta apagam o banco local |
| Manutenibilidade | Camadas `ui` / `data` / `domain` / `sync` / `util`; regras de negócio em Java puro |
| Tratamento de erros | `ApiClient.mensagemDeErro` lê os erros do Supabase (REST: `code/message/details/hint`; Auth: `error/error_description/msg`; antigo: `erro`): "E-mail ou senha incorretos", "Este e-mail já tem cadastro", sessão expirada, sem conexão |

## Regras de negócio (seção 3)

| Regra | Onde |
|---|---|
| Cada aluno vê só os próprios dados | Token em toda chamada; as funções RPC do Supabase filtram pelo aluno do token (RLS) |
| Presença única por aluno, evento e dia | **Garantida pelo servidor**: `validar_ingresso` + índice único no banco. O app não registra presença (`registrar_presencas` está bloqueada para alunos); só baixa e exibe |
| Curso de vários dias: o mesmo QR vale em qualquer dia do curso (uma presença por dia), só no horário do encontro daquele dia (±1h, fuso America/Sao_Paulo) | Servidor (`validar_ingresso`); o app só informa a janela (`CheckinValidator`) |
| QR do ingresso: `PE:INGRESSO:{inscricao.id}:{codigoIngresso}`, sem dado pessoal; só aparece depois que `rpc/inscrever` confirma | `domain/IngressoQr.java` |
| Badge de faculdade só com certificado emitido; totais do documento de fluxo (FECAP 7, São Camilo 3, PUC 3, ESPM 3, demais 1) | `BadgeCalculator`, `PerfilFragment` |
| Sessão: só entra com token válido do Supabase; 401 sem renovação apaga os dados locais e volta ao login | `AppRepository.login/cadastrar`, `ApiClient`, `ui/SessaoGuard` |
| Certificado só com critério cumprido (75% de presença) | Emitido pelo servidor; chega por `meus_certificados` e `listar_notificacoes` (tipo CERTIFICADO). O app mostra o progresso com `ParticipacaoCalculator` |
| Testes com perguntas, alternativas e critérios do banco | `rpc/listar_testes` → `TesteRepository` (reserva offline em `assets/testes.json`) |
| Resultado do teste não é diagnóstico | Aviso nas telas de testes e de resultado |
| Dados fictícios em testes e apresentações | Contas fictícias do grupo no Supabase (persona Ketlin) |
| LGPD: coleta mínima | Endereço, cidade e escola são opcionais; o cartão não mostra dados demográficos |

## Testes unitários (`app/src/test`)

31 testes JUnit (`./gradlew testDebugUnitTest`):

| Classe | Testes | O que cobre |
|---|---|---|
| `CheckinValidatorTest` | 9 | Janela informativa: 55 min antes/depois = NO_HORARIO, 1h30 antes = ANTES_DO_HORARIO (abre às 18:00), dia seguinte, curso de 2 dias, encontro após a meia-noite, evento externo |
| `IngressoQrTest` | 5 | Formato `PE:INGRESSO:{id}:{codigo}`, sem dado pessoal, sem QR para cancelada/externo, "sincronizando" offline |
| `ErroSupabaseTest` | 4 | Login inválido, e-mail já cadastrado, JWT expirado/401, regra de negócio do banco |
| `ParticipacaoCalculatorTest` | 3 | Percentual e 75% |
| `ResumoParticipacaoTest` | 2 | Presenças, faltas e futuros |
| `BadgeCalculatorTest` | 6 | Níveis por faculdade (só com certificado; Alvarista = 7 cursos), nomes sem acento, jornada |
| `TesteScorerTest` | 2 | Pontuação DISC/RIASEC |
