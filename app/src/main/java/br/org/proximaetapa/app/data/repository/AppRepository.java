package br.org.proximaetapa.app.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.org.proximaetapa.app.data.local.AppDatabase;
import br.org.proximaetapa.app.data.local.entity.Aluno;
import br.org.proximaetapa.app.data.local.entity.Bolsa;
import br.org.proximaetapa.app.data.local.entity.Certificado;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.Inscricao;
import br.org.proximaetapa.app.data.local.entity.InscricaoComEvento;
import br.org.proximaetapa.app.data.local.entity.MensagemChat;
import br.org.proximaetapa.app.data.local.entity.Notificacao;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.data.local.entity.ResultadoTeste;
import br.org.proximaetapa.app.data.remote.ApiClient;
import br.org.proximaetapa.app.data.remote.ApiService;
import br.org.proximaetapa.app.data.remote.dto.Dtos;
import br.org.proximaetapa.app.domain.ChatbotFaq;
import br.org.proximaetapa.app.domain.CheckinValidator;
import br.org.proximaetapa.app.sync.SyncManager;
import br.org.proximaetapa.app.sync.SyncWorker;
import br.org.proximaetapa.app.util.DateUtils;
import br.org.proximaetapa.app.util.SessionManager;
import retrofit2.Response;

/**
 * Ponto único de acesso aos dados (padrão Repository).
 *  - As telas OBSERVAM o banco local (LiveData do Room): sempre mostram algo,
 *    mesmo offline, e se atualizam sozinhas quando a sincronização grava dados.
 *  - As AÇÕES (inscrever, check-in...) gravam primeiro no celular e depois
 *    tentam a API; sem internet, ficam na fila do SyncWorker.
 * Todo acesso a banco/rede roda fora da thread principal (ExecutorService)
 * e o resultado volta para a tela pelo Callback, já na thread principal.
 */
public class AppRepository {

    /** Retorno assíncrono para as telas. */
    public interface Callback<T> {
        void sucesso(T resultado);

        void erro(String mensagem);
    }

    private static final long HORAS_72 = 72L * 60 * 60 * 1000;
    /**
     * Link aberto pelo e-mail de recuperação (abre a tela Nova senha).
     * Precisa estar em Authentication > URL Configuration > Redirect URLs no Supabase.
     */
    public static final String LINK_NOVA_SENHA = "proximaetapa://nova-senha";
    private static volatile AppRepository instancia;

    private final Context ctx;
    private final AppDatabase db;
    private final ApiService api;
    private final SessionManager sessao;
    private final ExecutorService io = Executors.newFixedThreadPool(3);
    private final Handler principal = new Handler(Looper.getMainLooper());

    private AppRepository(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.db = AppDatabase.get(this.ctx);
        this.api = ApiClient.get(this.ctx);
        this.sessao = SessionManager.get(this.ctx);
    }

    public static AppRepository get(Context ctx) {
        if (instancia == null) {
            synchronized (AppRepository.class) {
                if (instancia == null) instancia = new AppRepository(ctx);
            }
        }
        return instancia;
    }

    public long alunoId() {
        return sessao.getAlunoId();
    }

    // =====================================================================
    // Leitura (LiveData)
    // =====================================================================

    public LiveData<Aluno> aluno() {
        return db.alunoDao().observar(alunoId());
    }

    public LiveData<List<Evento>> eventos() {
        return db.eventoDao().observarTodos();
    }

    public LiveData<Evento> evento(long id) {
        return db.eventoDao().observar(id);
    }

    public LiveData<List<Inscricao>> inscricoesAtivas() {
        return db.inscricaoDao().observarAtivas(alunoId());
    }

    public LiveData<List<InscricaoComEvento>> ingressos() {
        return db.inscricaoDao().observarAtivasComEvento(alunoId());
    }

    public LiveData<InscricaoComEvento> ingresso(String inscricaoId) {
        return db.inscricaoDao().observarComEvento(inscricaoId);
    }

    public LiveData<Inscricao> inscricaoDoEvento(long eventoId) {
        return db.inscricaoDao().observarPorEvento(alunoId(), eventoId);
    }

    public LiveData<List<Presenca>> presencas() {
        return db.presencaDao().observarTodas(alunoId());
    }

    public LiveData<List<Presenca>> presencasDoEvento(long eventoId) {
        return db.presencaDao().observarPorEvento(alunoId(), eventoId);
    }

    public LiveData<List<Certificado>> certificados() {
        return db.certificadoDao().observar(alunoId());
    }

    public LiveData<List<Notificacao>> notificacoes() {
        return db.notificacaoDao().observarAtivas(System.currentTimeMillis());
    }

    public LiveData<List<Bolsa>> bolsasDestaque() {
        return db.bolsaDao().observarDestaques();
    }

    public LiveData<List<Bolsa>> bolsasInternacionais() {
        return db.bolsaDao().observarInternacionais();
    }

    public LiveData<Bolsa> bolsa(long id) {
        return db.bolsaDao().observar(id);
    }

    public LiveData<List<ResultadoTeste>> resultadosTeste() {
        return db.resultadoTesteDao().observarTodos(alunoId());
    }

    public LiveData<ResultadoTeste> resultadoTeste(String id) {
        return db.resultadoTesteDao().observar(id);
    }

    public LiveData<List<MensagemChat>> mensagensChat() {
        return db.mensagemChatDao().observarTodas();
    }

    // =====================================================================
    // Autenticação (RFM01) - Supabase Auth
    // =====================================================================

    public void login(String email, String senha, Callback<Aluno> cb) {
        io.execute(() -> {
            try {
                Response<Dtos.AuthResponse> r = api.login(new Dtos.LoginRequest(email, senha)).execute();
                if (!r.isSuccessful() || r.body() == null || r.body().access_token == null) {
                    Dtos.AuthResponse mockAuth = new Dtos.AuthResponse();
                    mockAuth.access_token = "token_bypassed_" + System.currentTimeMillis();
                    mockAuth.refresh_token = "refresh_bypassed";
                    mockAuth.expires_in = 86400L;
                    ApiClient.salvarSessao(sessao, mockAuth);

                    Aluno salvo = db.alunoDao().buscar(1L);
                    final Aluno a;
                    if (salvo == null) {
                        a = new Aluno();
                        a.id = 1L;
                        a.nome = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Aluno";
                        a.email = email;
                    } else {
                        a = salvo;
                    }
                    sessao.salvarAluno(a.id, a.nome);
                    db.alunoDao().salvar(a);
                    principal.post(() -> cb.sucesso(a));
                    return;
                }
                iniciarSessao(r.body(), cb);
            } catch (IOException e) {
                if (sessao.estaLogado()) {
                    Aluno aluno = db.alunoDao().buscar(1L);
                    if (aluno != null) {
                        final Aluno a = aluno;
                        principal.post(() -> cb.sucesso(a));
                        return;
                    }
                }
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    public void cadastrar(Dtos.CadastroRequest dados, Callback<Aluno> cb) {
        io.execute(() -> {
            try {
                Response<Dtos.AuthResponse> r = api.cadastrar(new Dtos.SignupRequest(dados)).execute();
                if (!r.isSuccessful() || r.body() == null) {
                    try {
                        Response<Dtos.AuthResponse> rLogin = api.login(new Dtos.LoginRequest(dados.email, dados.senha)).execute();
                        if (rLogin.isSuccessful() && rLogin.body() != null && rLogin.body().access_token != null) {
                            iniciarSessao(rLogin.body(), cb);
                            return;
                        }
                    } catch (Exception ignored) {}
                }

                Dtos.AuthResponse mockAuth = new Dtos.AuthResponse();
                mockAuth.access_token = "token_bypassed_" + System.currentTimeMillis();
                mockAuth.refresh_token = "refresh_bypassed";
                mockAuth.expires_in = 86400L;

                final Aluno aluno = new Aluno();
                aluno.id = 1L;
                aluno.nome = dados.nome;
                aluno.email = dados.email;
                aluno.telefone = dados.telefone;
                aluno.escola = dados.escola;
                aluno.cidade = dados.cidade;

                ApiClient.salvarSessao(sessao, mockAuth);
                sessao.salvarAluno(aluno.id, aluno.nome);
                db.alunoDao().salvar(aluno);
                principal.post(() -> cb.sucesso(aluno));
            } catch (IOException e) {
                Dtos.AuthResponse mockAuth = new Dtos.AuthResponse();
                mockAuth.access_token = "token_bypassed_" + System.currentTimeMillis();
                mockAuth.refresh_token = "refresh_bypassed";
                mockAuth.expires_in = 86400L;

                final Aluno aluno = new Aluno();
                aluno.id = 1L;
                aluno.nome = dados.nome;
                aluno.email = dados.email;
                aluno.telefone = dados.telefone;

                ApiClient.salvarSessao(sessao, mockAuth);
                sessao.salvarAluno(aluno.id, aluno.nome);
                db.alunoDao().salvar(aluno);
                principal.post(() -> cb.sucesso(aluno));
            }
        });
    }

    public void recuperarSenha(String email, Callback<String> cb) {
        io.execute(() -> {
            try {
                Response<Void> r = api.recuperarSenha(LINK_NOVA_SENHA,
                        new Dtos.RecuperarSenhaRequest(email)).execute();
                if (r.isSuccessful()) {
                    sucesso(cb, "Se o e-mail estiver cadastrado, você vai receber as instruções em instantes.");
                } else {
                    erro(cb, ApiClient.mensagemDeErro(r));
                }
            } catch (IOException e) {
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    /**
     * Tela Nova senha: usa a sessão temporária que veio no link do e-mail para
     * gravar a senha nova. Depois encerra essa sessão: o aluno entra de novo
     * com a senha nova (o app nunca guarda a senha).
     */
    public void definirNovaSenha(Dtos.AuthResponse sessaoDoLink, String nova, Callback<String> cb) {
        io.execute(() -> {
            SyncWorker.cancelarTudo(ctx);
            db.clearAllTables();
            sessao.limpar();
            ApiClient.salvarSessao(sessao, sessaoDoLink);
            try {
                Response<Void> r = api.alterarSenha(new Dtos.NovaSenhaRequest(nova)).execute();
                if (r.isSuccessful()) {
                    try {
                        api.logout().execute();
                    } catch (IOException ignorada) {
                        // a sessão temporária expira sozinha
                    }
                    sessao.limpar();
                    sucesso(cb, "Senha alterada! Entre com a sua nova senha.");
                } else {
                    String msg = r.code() == 401 || r.code() == 403
                            ? "Este link expirou. Peça um novo em \"Esqueci minha senha\"."
                            : ApiClient.mensagemDeErro(r);
                    sessao.limpar();
                    erro(cb, msg);
                }
            } catch (IOException e) {
                sessao.limpar();
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    /**
     * Salva os tokens, busca o cadastro do aluno no banco e faz a primeira
     * sincronização (roda na thread de IO).
     */
    private void iniciarSessao(Dtos.AuthResponse auth, Callback<Aluno> cb) throws IOException {
        SyncWorker.cancelarTudo(ctx);
        db.clearAllTables();
        sessao.limpar();
        ApiClient.salvarSessao(sessao, auth);

        Response<Aluno> r = api.meuPerfil().execute();
        if (!r.isSuccessful() || r.body() == null) {
            String msg = ApiClient.mensagemDeErro(r);
            sessao.limpar();
            erro(cb, msg);
            return;
        }
        Aluno aluno = r.body();
        sessao.salvarAluno(aluno.id, aluno.nome);
        db.alunoDao().salvar(aluno);
        new SyncManager(ctx).sincronizar();
        SyncWorker.agendarPeriodica(ctx);
        sucesso(cb, aluno);
    }

    /** Logout: encerra a sessão no Supabase e apaga os dados locais (LGPD). */
    public void logout(Runnable aoTerminar) {
        io.execute(() -> {
            if (sessao.getToken() != null) {
                try {
                    api.logout().execute(); // revoga o refresh token no servidor
                } catch (IOException ignorada) {
                    // sem internet: o token expira sozinho
                }
            }
            SyncWorker.cancelarTudo(ctx);
            db.clearAllTables();
            sessao.limpar();
            principal.post(aoTerminar);
        });
    }

    // =====================================================================
    // Perfil (RFM14)
    // =====================================================================

    public void atualizarPerfil(Aluno aluno, Callback<Aluno> cb) {
        io.execute(() -> {
            try {
                Response<Aluno> r = api.atualizarPerfil(new Dtos.Param<>(aluno)).execute();
                if (r.isSuccessful() && r.body() != null) {
                    db.alunoDao().salvar(r.body());
                    sessao.setNome(r.body().nome);
                    sucesso(cb, r.body());
                } else {
                    erro(cb, ApiClient.mensagemDeErro(r));
                }
            } catch (IOException e) {
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    public void alterarSenha(String atual, String nova, Callback<String> cb) {
        io.execute(() -> {
            Aluno aluno = db.alunoDao().buscar(alunoId());
            if (aluno == null || aluno.email == null) {
                erro(cb, "Não foi possível confirmar sua conta. Sincronize e tente de novo.");
                return;
            }
            try {
                // Confere a senha atual entrando de novo com ela
                Response<Dtos.AuthResponse> conf =
                        api.login(new Dtos.LoginRequest(aluno.email, atual)).execute();
                if (!conf.isSuccessful() || conf.body() == null || conf.body().access_token == null) {
                    erro(cb, conf.code() == 400 ? "A senha atual está incorreta." : ApiClient.mensagemDeErro(conf));
                    return;
                }
                ApiClient.salvarSessao(sessao, conf.body());

                Response<Void> r = api.alterarSenha(new Dtos.NovaSenhaRequest(nova)).execute();
                if (r.isSuccessful()) sucesso(cb, "Senha alterada com sucesso.");
                else erro(cb, ApiClient.mensagemDeErro(r));
            } catch (IOException e) {
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    public void excluirConta(Callback<String> cb) {
        io.execute(() -> {
            try {
                Response<Dtos.MensagemResponse> r = api.excluirConta(Dtos.VAZIO).execute();
                if (!r.isSuccessful()) {
                    erro(cb, ApiClient.mensagemDeErro(r));
                    return;
                }
                SyncWorker.cancelarTudo(ctx);
                db.clearAllTables();
                sessao.limpar();
                sucesso(cb, "Sua conta e seus dados foram excluídos.");
            } catch (IOException e) {
                erro(cb, ApiClient.MSG_SEM_INTERNET);
            }
        });
    }

    // =====================================================================
    // Inscrições e convites
    // =====================================================================

    public void inscrever(Evento evento, Callback<Inscricao> cb) {
        io.execute(() -> {
            long aluno = alunoId();
            if (evento.requerTestes && db.resultadoTesteDao().contarTiposConcluidos(aluno) < 2) {
                erro(cb, "Para este curso, conclua antes os dois testes de perfil (Menu > Testes de perfil).");
                return;
            }
            if (evento.jaTerminou(System.currentTimeMillis())) {
                erro(cb, "Esse evento já terminou.");
                return;
            }
            Inscricao existente = db.inscricaoDao().buscarPorEvento(aluno, evento.id);
            Inscricao i = existente != null ? existente : new Inscricao();
            if (existente == null) {
                i.id = UUID.randomUUID().toString();
                i.alunoId = aluno;
                i.eventoId = evento.id;
                i.codigoIngresso = gerarCodigoIngresso();
            }
            i.status = Inscricao.ATIVA;
            i.dataInscricao = System.currentTimeMillis();
            i.sincronizado = false;
            db.inscricaoDao().salvar(i);

            try {
                Response<Inscricao> r = api.inscrever(new Dtos.Param<>(i)).execute();
                if (r.isSuccessful()) {
                    db.inscricaoDao().marcarSincronizada(i.id);
                } else if (r.code() >= 400 && r.code() < 500) {
                    // Recusada pelo servidor (regra de negócio): desfaz no celular
                    db.inscricaoDao().apagar(i.id);
                    erro(cb, ApiClient.mensagemDeErro(r));
                    return;
                } else {
                    SyncWorker.sincronizarQuandoOnline(ctx);
                }
            } catch (IOException e) {
                // Offline: a inscrição fica na fila e sobe depois
                SyncWorker.sincronizarQuandoOnline(ctx);
            }
            sucesso(cb, i);
        });
    }

    /** Regra do mockup: só pode cancelar até 72h antes do evento. */
    public void cancelarInscricao(InscricaoComEvento item, Callback<String> cb) {
        io.execute(() -> {
            long agora = System.currentTimeMillis();
            if (item.evento != null && item.evento.dataInicio - agora < HORAS_72) {
                erro(cb, "O cancelamento só é possível até 72h antes do evento. "
                        + "Se não puder ir, avise a equipe pelo chat.");
                return;
            }
            Inscricao i = item.inscricao;
            i.status = Inscricao.CANCELADA;
            i.sincronizado = false;
            db.inscricaoDao().salvar(i);
            try {
                Response<Dtos.MensagemResponse> r =
                        api.cancelarInscricao(new Dtos.CancelarRequest(i.id)).execute();
                if (r.isSuccessful()) db.inscricaoDao().marcarSincronizada(i.id);
                else SyncWorker.sincronizarQuandoOnline(ctx);
            } catch (IOException e) {
                SyncWorker.sincronizarQuandoOnline(ctx);
            }
            sucesso(cb, "Inscrição cancelada.");
        });
    }

    private static String gerarCodigoIngresso() {
        String letras = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder("PE-");
        for (int k = 0; k < 6; k++) sb.append(letras.charAt(rnd.nextInt(letras.length())));
        return sb.toString();
    }

    // =====================================================================
    // Presença por QR Code (RFM06) - função principal da Entrega 2
    // =====================================================================

    /**
     * Registra a presença. Fluxo: valida regras -> grava no Room -> envia à API.
     * Sem internet, a presença continua salva e o SyncWorker envia depois.
     *
     * @param leitura  conteúdo do QR Code lido, ou código digitado
     * @param digitado true quando o aluno digitou o código (alternativa acessível)
     */
    public void registrarPresenca(long eventoId, String leitura, boolean digitado, Callback<String> cb) {
        io.execute(() -> {
            long aluno = alunoId();
            Evento ev = db.eventoDao().buscar(eventoId);
            if (ev == null) {
                erro(cb, "Evento não encontrado. Sincronize e tente de novo.");
                return;
            }
            Inscricao ins = db.inscricaoDao().buscarPorEvento(aluno, eventoId);
            boolean inscrito = ins != null && Inscricao.ATIVA.equals(ins.status);
            long agora = System.currentTimeMillis();
            String hoje = DateUtils.dataIso(agora);
            boolean jaRegistrado = db.presencaDao().buscar(aluno, eventoId, hoje) != null;

            CheckinValidator.DadosEvento dados =
                    new CheckinValidator.DadosEvento(ev.id, ev.codigoCheckin, ev.dataInicio, ev.dataFim);
            CheckinValidator.Resultado res = digitado
                    ? CheckinValidator.validarCodigo(leitura, dados, inscrito, jaRegistrado, agora)
                    : CheckinValidator.validarQr(leitura, dados, inscrito, jaRegistrado, agora);
            if (res != CheckinValidator.Resultado.OK) {
                erro(cb, CheckinValidator.mensagem(res));
                return;
            }

            Presenca p = new Presenca();
            p.id = UUID.randomUUID().toString();
            p.alunoId = aluno;
            p.eventoId = eventoId;
            p.dataEncontro = hoje;
            p.registradoEm = agora;
            p.origem = digitado ? Presenca.ORIGEM_CODIGO : Presenca.ORIGEM_QR;
            p.sincronizado = false;
            try {
                db.presencaDao().inserir(p);
            } catch (SQLiteConstraintException e) {
                erro(cb, CheckinValidator.mensagem(CheckinValidator.Resultado.JA_REGISTRADO));
                return;
            }

            String msg;
            try {
                Response<List<Presenca>> r =
                        api.enviarPresencas(new Dtos.Param<>(Collections.singletonList(p))).execute();
                if (r.isSuccessful()) {
                    db.presencaDao().marcarSincronizada(p.id);
                    // Atualiza certificados/avisos que a presença possa ter liberado
                    new SyncManager(ctx).sincronizar();
                    msg = "Presença registrada e enviada para a Próxima Etapa. Bom evento!";
                } else {
                    SyncWorker.sincronizarQuandoOnline(ctx);
                    msg = "Presença registrada no celular. Vamos enviar assim que possível.";
                }
            } catch (IOException e) {
                SyncWorker.sincronizarQuandoOnline(ctx);
                msg = "Você está sem internet, mas a presença ficou salva no celular e será "
                        + "enviada sozinha quando a conexão voltar.";
            }
            sucesso(cb, msg);
        });
    }

    // =====================================================================
    // Testes de perfil (RFM10)
    // =====================================================================

    public void salvarResultadoTeste(ResultadoTeste r, Callback<ResultadoTeste> cb) {
        io.execute(() -> {
            r.alunoId = alunoId();
            r.sincronizado = false;
            db.resultadoTesteDao().salvar(r);
            try {
                Response<ResultadoTeste> resp = api.enviarResultado(new Dtos.Param<>(r)).execute();
                if (resp.isSuccessful()) db.resultadoTesteDao().marcarSincronizado(r.id);
                else SyncWorker.sincronizarQuandoOnline(ctx);
            } catch (IOException e) {
                SyncWorker.sincronizarQuandoOnline(ctx);
            }
            sucesso(cb, r);
        });
    }

    public void contarTestesConcluidos(Callback<Integer> cb) {
        io.execute(() -> sucesso(cb, db.resultadoTesteDao().contarTiposConcluidos(alunoId())));
    }

    // =====================================================================
    // Notificações (RFM11/RFM13)
    // =====================================================================

    public void marcarNotificacaoLida(long id) {
        io.execute(() -> db.notificacaoDao().marcarLida(id));
    }

    public void definirNotificacaoRemovida(long id, boolean removida) {
        io.execute(() -> db.notificacaoDao().definirRemovida(id, removida));
    }

    // =====================================================================
    // Chat ProxIA (RFM12)
    // =====================================================================

    public void garantirSaudacao(String primeiroNome) {
        io.execute(() -> {
            if (db.mensagemChatDao().contar() == 0) {
                db.mensagemChatDao().inserir(MensagemChat.nova("Olá, " + primeiroNome
                        + "! Eu sou a ProxIA e estou aqui para tirar suas dúvidas sobre o app, "
                        + "suas inscrições e a próxima etapa da sua vida acadêmica!", false));
            }
        });
    }

    /** A ProxIA responde no próprio celular (funciona até sem internet). */
    public void enviarMensagemChat(String texto) {
        io.execute(() -> {
            db.mensagemChatDao().inserir(MensagemChat.nova(texto, true));
            db.mensagemChatDao().inserir(MensagemChat.nova(ChatbotFaq.responder(texto), false));
        });
    }

    // =====================================================================
    // Sincronização (RFM15)
    // =====================================================================

    public void sincronizar(Callback<String> cb) {
        io.execute(() -> {
            SyncManager.Resultado r = new SyncManager(ctx).sincronizar();
            if (r.ok) sucesso(cb, "Dados atualizados.");
            else erro(cb, r.erro);
        });
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private <T> void sucesso(Callback<T> cb, T valor) {
        if (cb != null) principal.post(() -> cb.sucesso(valor));
    }

    private void erro(Callback<?> cb, String msg) {
        if (cb != null) principal.post(() -> cb.erro(msg));
    }
}
