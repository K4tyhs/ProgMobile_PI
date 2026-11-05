# Próxima Etapa — App do Aluno (Android)

Projeto Interdisciplinar · 3º ADS · FECAP · Grupo 5
Laura Piveta Pelizzer · Matheus Marques Quio · Katiely de Jesus Silva · Danilo Oliveira de Almeida

Aplicativo Android (Java + XML, Material Design 3) para os alunos da ONG Próxima Etapa: notificações, cursos e palestras, calendário, ingresso com QR Code, histórico de presença, certificados, testes de perfil, bolsas e chat de dúvidas. As telas seguem o mockup do Figma "Próxima Etapa — Design System".

Pacote: `com.example.proximaetapa`.

## Como abrir e rodar

1. Android Studio → **File > Open** → selecione a pasta `ProximaEtapaApp`.
2. Em *Settings > Build, Execution, Deployment > Build Tools > Gradle*, use um **Gradle JDK 17 ou 21**. Com JDK 23 ou mais novo o Gradle 8.9 do projeto não roda ("Unsupported class file major version").
3. Espere o Gradle sincronizar e rode em um emulador ou celular com **Android 7.0 (API 24) ou superior**, com internet.
4. Entre com uma conta fictícia do grupo cadastrada no Supabase (ou crie uma em "Cadastrar conta nova").

## Backend (Supabase)

O app usa o Supabase do projeto, configurado em `app/build.gradle` (`SUPABASE_URL` e a chave **pública** `SUPABASE_KEY`):

- **Login e cadastro:** Supabase Auth (`auth/v1/token`, `auth/v1/signup`). O app só entra com uma sessão válida; os tokens ficam criptografados e são renovados sozinhos.
- **Dados do aluno:** funções SQL chamadas por `POST rest/v1/rpc/<nome>` (lista em `data/remote/ApiService.java`). Cada função devolve só os dados do aluno do token.
- **Presença:** o app mostra um QR Code único por inscrição (`PE:INGRESSO:{id}:{codigoIngresso}`). O leitor web da Próxima Etapa valida o QR na entrada (`validar_ingresso`) e o app recebe a presença confirmada na sincronização.

`BuildConfig.MODO_DEMO` fica `false`: não há modo demonstração sem servidor.

## Arquitetura

```
ui (Activities/Fragments/Adapters)
      │ observa LiveData            │ chama ações
      ▼                             ▼
AppRepository ──► Room (SQLite local) ◄── SyncManager / SyncWorker
      │                                        │
      └──────────── Retrofit (ApiService) ◄────┘
                         │
                     Supabase
```

- `ui/`: telas. `data/`: Room, Retrofit e repositórios. `domain/`: regras de negócio em Java puro (janela de check-in, QR do ingresso, participação, badges, pontuação dos testes). `sync/`: sincronização (ao abrir o app, quando a internet volta e a cada 15 min). `util/`: utilitários.
- `assets/testes.json`: cópia das perguntas dos testes de perfil, usada enquanto o app ainda não baixou a versão do servidor (`listar_testes`).
- Testes unitários: botão direito em `app/src/test` → **Run Tests** (ou `./gradlew testDebugUnitTest`).

## Requisitos do PDP

A rastreabilidade (Entrega 2, RFM01 a RFM15, requisitos não funcionais e regras de negócio, cada um apontando para o código) está em [`docs/REQUISITOS.md`](docs/REQUISITOS.md).

## Imagens

As fachadas sem foto no mockup usam capas coloridas por instituição (`CapaUtils`). Para usar o logo oficial, coloque `logo_proxima_etapa.png` em `res/drawable` e troque o `view_logo.xml` por um `ImageView`.
