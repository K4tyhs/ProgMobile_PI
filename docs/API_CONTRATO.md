# Contrato da API — Próxima Etapa (App do Aluno)

O app Android já consome estas rotas via Retrofit (`ApiService.java`). O backend (Node + Express, Spring ou .NET) precisa responder **exatamente** neste formato para trocar o modo demonstração pela API real.

- Base: `http://10.0.2.2:3000/api/` no emulador (definida em `app/build.gradle` → `API_BASE_URL`).
- JSON em UTF-8. Datas em **milissegundos (epoch)**, exceto `dataEncontro` (`yyyy-MM-dd`).
- Todas as rotas, menos `/auth/*`, exigem `Authorization: Bearer <token>` e devolvem **só os dados do aluno do token** (LGPD).
- Erros: status HTTP 4xx/5xx com corpo `{"erro": "mensagem amigável para o aluno"}`. O app mostra esse texto.
- **401** em qualquer rota fora de `/auth/*` = token inválido ou expirado: o app apaga os dados locais e volta para o login.

## Autenticação (RFM01)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `auth/login` | `{email, senha}` | `{token, aluno}` · 401 se inválido |
| POST | `auth/cadastro` | `{nome, email, endereco, cidade, escola, senha}` | `{token, aluno}` · 409 se e-mail existe |
| POST | `auth/recuperar-senha` | `{email}` | `{mensagem}` |

## Aluno / perfil (RFM14)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `alunos/me` | — | `Aluno` |
| PUT | `alunos/me` | `Aluno` (id, email e matrícula são ignorados) | `Aluno` |
| PUT | `alunos/me/senha` | `{senhaAtual, novaSenha}` | `{mensagem}` · 400 se senha atual errada |
| DELETE | `alunos/me` | — | `{mensagem}` (exclusão LGPD) |

```json
{ "id": 1, "nome": "Ketlin Silva dos Santos", "email": "ketlin@proximaetapa.org",
  "matricula": "PE-000001", "linkedin": "linkedin.com/in/ketlinsilva",
  "endereco": "...", "cidade": "...", "escola": "...",
  "interesses": "Vestibular,Bolsas de estudo" }
```

## Eventos: cursos, palestras e externos (RFM03/RFM05)

`GET eventos` → lista de:

```json
{ "id": 21, "titulo": "Currículo criativo: Workshop", "descricao": "...",
  "instituicao": "ESPM", "local": "ESPM - Vila Mariana", "tipo": "PALESTRA",
  "categoria": "Carreira", "dataInicio": 1794672000000, "dataFim": 1794682800000,
  "cargaHoraria": 3, "totalEncontros": 1, "linkExterno": null,
  "codigoCheckin": "CURRICULO21", "requerTestes": false }
```

`tipo`: `CURSO`, `PALESTRA` ou `EXTERNO` (vestibular/ENEM, com `linkExterno`).

## Inscrições / ingressos

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `inscricoes` | — | lista de `Inscricao` (inclui canceladas) |
| POST | `inscricoes` | `Inscricao` | `Inscricao` · 422 se regra impedir (ex.: `requerTestes`) |
| DELETE | `inscricoes/{id}` | — | `{mensagem}` (marca `status = CANCELADA`) |

```json
{ "id": "uuid-gerado-no-celular", "alunoId": 1, "eventoId": 21,
  "dataInscricao": 1794000000000, "status": "ATIVA", "codigoIngresso": "PE-7KQ2MD" }
```

O `id` é um UUID criado no celular (permite inscrição offline). POST com o mesmo aluno+evento deve **reativar** a inscrição, não duplicar.

## Presença por QR Code (RFM06/RFM07)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| GET | `presencas` | — | lista de `Presenca` |
| POST | `presencas` | **lista** de `Presenca` (envio em lote da fila offline) | lista aceita |

```json
{ "id": "uuid", "alunoId": 1, "eventoId": 4, "dataEncontro": "2026-11-03",
  "registradoEm": 1793740000000, "origem": "QR" }
```

Regras que o servidor também deve aplicar: única por `(alunoId, eventoId, dataEncontro)` (repetida = ignora e devolve a existente), aluno inscrito, dentro do horário. Depois de gravar, emitir certificado se presença ≥ 75% dos encontros e criar uma notificação `CERTIFICADO`.

QR Code exibido no evento: `PE:CHECKIN:{eventoId}:{codigoCheckin}`.

## Certificados (RFM09), bolsas e notificações (RFM04/11/13)

- `GET certificados` → `{id, alunoId, eventoId, titulo, instituicao, cargaHoraria, dataEmissao, codigoValidacao}`
- `GET bolsas` → `{id, nome, instituicao, descricao, requisitos, duracao, prazo, link, internacional, destaque}`
- `GET notificacoes` → `{id, titulo, mensagem, tipo, eventoId, dataPublicacao, expiraEm}` · `tipo`: `PALESTRA`, `CURSO`, `EXTERNO`, `CERTIFICADO`, `AVISO`. Para grupos específicos, filtre no servidor.

## Testes de perfil (RFM10) e chat (RFM12)

- `GET testes` → lista com perguntas, alternativas e critérios de classificação cadastrados no banco (mesmo formato de `app/src/main/assets/testes.json`). O app salva no celular e usa offline.
- `POST testes/resultados` ← `{id, tipo: "DISC"|"RIASEC", codigo, perfil, pontuacoesJson, data}` → mesmo objeto
- `POST chat` ← `{mensagem}` → `{resposta}`

## Tabelas sugeridas (adaptar ao banco fornecido pela ONG)

```sql
CREATE TABLE alunos (
  id SERIAL PRIMARY KEY, nome VARCHAR(120) NOT NULL, email VARCHAR(160) UNIQUE NOT NULL,
  senha_hash VARCHAR(100) NOT NULL, matricula VARCHAR(20) UNIQUE, linkedin VARCHAR(200),
  endereco VARCHAR(200), cidade VARCHAR(100), escola VARCHAR(150), interesses VARCHAR(300)
);
CREATE TABLE eventos (
  id SERIAL PRIMARY KEY, titulo VARCHAR(150) NOT NULL, descricao TEXT, instituicao VARCHAR(80),
  local VARCHAR(150), tipo VARCHAR(10) CHECK (tipo IN ('CURSO','PALESTRA','EXTERNO')),
  categoria VARCHAR(50), data_inicio TIMESTAMP NOT NULL, data_fim TIMESTAMP NOT NULL,
  carga_horaria INT DEFAULT 0, total_encontros INT DEFAULT 1, link_externo VARCHAR(300),
  codigo_checkin VARCHAR(30), requer_testes BOOLEAN DEFAULT FALSE
);
CREATE TABLE inscricoes (
  id UUID PRIMARY KEY, aluno_id INT REFERENCES alunos(id) ON DELETE CASCADE,
  evento_id INT REFERENCES eventos(id), data_inscricao TIMESTAMP NOT NULL,
  status VARCHAR(10) NOT NULL, codigo_ingresso VARCHAR(12) NOT NULL,
  UNIQUE (aluno_id, evento_id)
);
CREATE TABLE presencas (
  id UUID PRIMARY KEY, aluno_id INT REFERENCES alunos(id) ON DELETE CASCADE,
  evento_id INT REFERENCES eventos(id), data_encontro DATE NOT NULL,
  registrado_em TIMESTAMP NOT NULL, origem VARCHAR(10),
  UNIQUE (aluno_id, evento_id, data_encontro)
);
CREATE TABLE certificados (
  id SERIAL PRIMARY KEY, aluno_id INT REFERENCES alunos(id) ON DELETE CASCADE,
  evento_id INT REFERENCES eventos(id), data_emissao TIMESTAMP NOT NULL,
  codigo_validacao VARCHAR(20) UNIQUE NOT NULL, UNIQUE (aluno_id, evento_id)
);
CREATE TABLE bolsas (
  id SERIAL PRIMARY KEY, nome VARCHAR(150), instituicao VARCHAR(100), descricao TEXT,
  requisitos TEXT, duracao VARCHAR(60), prazo VARCHAR(100), link VARCHAR(300),
  internacional BOOLEAN DEFAULT FALSE, destaque BOOLEAN DEFAULT FALSE
);
CREATE TABLE notificacoes (
  id SERIAL PRIMARY KEY, titulo VARCHAR(150), mensagem TEXT, tipo VARCHAR(12),
  evento_id INT REFERENCES eventos(id), data_publicacao TIMESTAMP NOT NULL, expira_em TIMESTAMP,
  publico VARCHAR(30) DEFAULT 'TODOS'
);
CREATE TABLE resultados_teste (
  id UUID PRIMARY KEY, aluno_id INT REFERENCES alunos(id) ON DELETE CASCADE,
  tipo VARCHAR(10), codigo VARCHAR(4), perfil VARCHAR(60), pontuacoes JSONB, data TIMESTAMP
);
```

Atenção: o banco devolve `snake_case` e timestamp; a API deve converter para os nomes em `camelCase` e datas em milissegundos mostrados acima. Use consultas parametrizadas (nunca concatenar SQL) e senha com hash (bcrypt).
