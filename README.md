# Próxima Etapa — App do Aluno (Android)

Projeto Interdisciplinar · 3º ADS · FECAP · Grupo 5
Laura Piveta Pelizzer · Matheus Marques Quio · Katiely de Jesus Silva · Danilo Oliveira de Almeida

Aplicativo Android (Java + XML, Material Design 3) para os alunos da ONG Próxima Etapa: notificações, cursos e palestras, calendário, convite com QR Code, registro de presença, certificados, testes de perfil, bolsas e chat de dúvidas. As telas seguem o mockup do Figma "Próxima Etapa — Design System".

## Como abrir e rodar

1. Android Studio (Koala ou mais novo) → **File > Open** → selecione a pasta `ProximaEtapaApp`.
2. Espere o Gradle sincronizar (baixa as bibliotecas na primeira vez).
3. Rode em um emulador ou celular com **Android 7.0 (API 24) ou superior**.
4. No modo demonstração, entre com:
   - E-mail: `ketlin@proximaetapa.org`
   - Senha: `123456`

> Se o Android Studio sugerir atualizar o Android Gradle Plugin, pode aceitar.

## Modo demonstração x API real

Em `app/build.gradle`:

```groovy
buildConfigField "boolean", "USE_MOCK_API", "true"          // true = sem servidor
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\""
```

- `true`: o `MockInterceptor` responde às chamadas do Retrofit com JSON de verdade, usando `assets/mock_db.json` (dados fictícios, com datas relativas ao dia de hoje). Todo o caminho Retrofit → Gson → Room → telas funciona.
- `false`: o app chama a API REST do projeto. O contrato completo está em [`docs/API_CONTRATO.md`](docs/API_CONTRATO.md).

## Roteiro de demonstração (banca)

1. **Login** → Home com "Está chegando…" (workshop de hoje), avisos e cursos. Use o filtro **Meus cursos** para ver os cursos inscritos com o % de presença.
2. **Calendário** → semana com eventos inscritos (roxo) e vestibulares (verde).
3. Home → card "Currículo criativo" → **Inscreva-se** → convite. Toque no convite para virar e ver o QR Code.
4. **Menu > Meus ingressos** → "Imagem pessoal e networking" (hoje) → **Registrar presença** → leia `docs/qrcodes_demo/checkin_evento_4.png` (ou digite o código `NETWORK4`). O certificado é liberado e chega uma notificação.
5. Ative o **modo avião** e registre outra presença: ela fica salva ("aguardando internet") e sobe sozinha quando a conexão voltar.
6. Em **Meus ingressos**, a seção **Participação nos cursos** mostra presenças, faltas e quanto falta para o certificado.
7. **Perfil** → cartão digital, badges e gráficos. **Menu > Meus certificados** → baixar PDF.
8. **Menu > Testes de perfil** → DISC e RIASEC → resultado. Depois disso o curso "Soft skills (PUC)" libera a inscrição.
9. **Menu > Configurações > Simular publicação de novo curso** ou **Simular mudança de horário** → chega a notificação no celular.
10. **Restaurar dados de demonstração** recalcula as datas para o dia da apresentação.

## Requisitos do PDP atendidos

A tabela completa (Entrega 2, RFM01 a RFM15, requisitos não funcionais e regras de negócio, cada um apontando para o código) está em [`docs/REQUISITOS.md`](docs/REQUISITOS.md).

Resumo: API REST (Retrofit + JSON), autenticação com sessão criptografada, Room (SQLite), sincronização com WorkManager e fila offline, presença por QR Code, notificações (novos cursos, mudança de agenda, certificados), histórico de presenças e faltas, card digital, certificados em PDF, testes de perfil vindos da API e chat. Todas as telas usam apenas ConstraintLayout.

## Arquitetura

```
ui (Activities/Fragments/Adapters)
      │ observa LiveData            │ chama ações
      ▼                             ▼
AppRepository ──► Room (SQLite local) ◄── SyncManager / SyncWorker
      │                                        │
      └──────────── Retrofit (ApiService) ◄────┘
                         │
          MockInterceptor (demo)  ou  API REST real
```

- `domain/`: regras de negócio em Java puro (check-in, participação, badges, pontuação dos testes) — cobertas por testes em `app/src/test`.
- Testes unitários (13): no Android Studio, botão direito em `app/src/test` → **Run Tests**.

## Imagens

As fotos das fachadas do mockup foram substituídas por capas coloridas por instituição (`CapaUtils`). Para usar o logo oficial, coloque `logo_proxima_etapa.png` em `res/drawable` e troque o `view_logo.xml` por um `ImageView`.
