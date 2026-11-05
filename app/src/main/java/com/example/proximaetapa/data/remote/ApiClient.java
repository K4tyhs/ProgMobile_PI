package com.example.proximaetapa.data.remote;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.example.proximaetapa.BuildConfig;
import com.example.proximaetapa.data.remote.dto.Dtos;
import com.example.proximaetapa.util.SessionManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Monta o cliente Retrofit do Supabase (uma instância para o app inteiro).
 *  - Toda chamada leva o cabeçalho "apikey" (chave PÚBLICA do projeto).
 *  - Chamadas do aluno logado levam "Authorization: Bearer {access token}".
 *  - O access token vence em ~1h: ele é renovado antes de vencer e, se a API
 *    ainda assim responder 401, o Authenticator renova e repete a chamada.
 *    Se a renovação for recusada, a sessão expirou e o app volta ao login.
 */
public final class ApiClient {

    private static final Gson GSON = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Object TRAVA_RENOVACAO = new Object();
    /** Renova o token quando faltar menos que isso para vencer. */
    private static final long MARGEM_MS = 60_000L;

    private static ApiService servico;

    private ApiClient() {
    }

    public static synchronized ApiService get(Context ctx) {
        if (servico == null) {
            SessionManager sessao = SessionManager.get(ctx.getApplicationContext());

            // Cliente "puro", usado só para renovar o token (sem interceptors)
            OkHttpClient base = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();

            // Segurança: registra só método, URL e status. O corpo NÃO é registrado,
            // porque o login e o cadastro levam a senha no corpo da requisição.
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);
            log.redactHeader("Authorization");
            log.redactHeader("apikey");

            OkHttpClient http = base.newBuilder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder req = original.newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_KEY);
                        if (usaTokenDoAluno(original)) {
                            String token = tokenValido(base, sessao);
                            if (token != null) req.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(req.build());
                    })
                    .authenticator((route, resposta) -> {
                        Request req = resposta.request();
                        String usado = tokenDoCabecalho(req);
                        if (!usaTokenDoAluno(req) || usado == null) return null;
                        // Já renovou e repetiu uma vez e ainda deu 401: a conta não
                        // tem mais acesso (ex.: excluída ou bloqueada) -> encerra a sessão
                        if (resposta.priorResponse() != null) {
                            sessao.marcarSessaoExpirada();
                            return null;
                        }
                        String novo = renovar(base, sessao, usado);
                        if (novo == null) {
                            // Supabase recusou o refresh token: sessão encerrada -> volta ao login
                            sessao.marcarSessaoExpirada();
                            return null;
                        }
                        return req.newBuilder().header("Authorization", "Bearer " + novo).build();
                    })
                    .addInterceptor(log)
                    .build();

            servico = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(http)
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build()
                    .create(ApiService.class);
        }
        return servico;
    }

    /** Guarda os tokens devolvidos pelo Supabase Auth. */
    public static void salvarSessao(SessionManager sessao, Dtos.AuthResponse a) {
        long expiraEm = a.expires_at > 0
                ? a.expires_at * 1000L
                : System.currentTimeMillis() + a.expires_in * 1000L;
        sessao.salvarTokens(a.access_token, a.refresh_token, expiraEm);
    }

    /** Rotas de login/cadastro/recuperação não usam o token do aluno. */
    private static boolean usaTokenDoAluno(Request req) {
        String caminho = req.url().encodedPath();
        return !caminho.endsWith("/auth/v1/token")
                && !caminho.endsWith("/auth/v1/signup")
                && !caminho.endsWith("/auth/v1/recover");
    }

    private static String tokenDoCabecalho(Request req) {
        String h = req.header("Authorization");
        return h != null && h.startsWith("Bearer ") ? h.substring(7) : null;
    }

    /** Token atual; se estiver para vencer, tenta renovar antes da chamada. */
    private static String tokenValido(OkHttpClient base, SessionManager sessao) {
        String token = sessao.getToken();
        if (token == null) return null;
        long expira = sessao.getTokenExpiraEm();
        if (expira > 0 && expira - System.currentTimeMillis() < MARGEM_MS) {
            try {
                String novo = renovar(base, sessao, token);
                if (novo != null) return novo;
                // Refresh recusado: a sessão acabou (a chamada vai dar 401 e o app volta ao login)
                sessao.marcarSessaoExpirada();
            } catch (IOException ignorada) {
                // sem internet: segue com o token atual (a chamada vai falhar por rede)
            }
        }
        return token;
    }

    /**
     * Troca o refresh token por um access token novo.
     *
     * @return o token novo, ou null se o Supabase recusou (sessão encerrada)
     * @throws IOException sem conexão (a sessão continua válida)
     */
    private static String renovar(OkHttpClient base, SessionManager sessao, String tokenUsado)
            throws IOException {
        synchronized (TRAVA_RENOVACAO) {
            // Outra thread pode ter acabado de renovar
            String atual = sessao.getToken();
            if (atual != null && !atual.equals(tokenUsado)
                    && sessao.getTokenExpiraEm() - System.currentTimeMillis() > MARGEM_MS) {
                return atual;
            }
            String refresh = sessao.getRefreshToken();
            if (refresh == null) return null;

            Request req = new Request.Builder()
                    .url(BuildConfig.SUPABASE_URL + "auth/v1/token?grant_type=refresh_token")
                    .header("apikey", BuildConfig.SUPABASE_KEY)
                    .post(RequestBody.create(GSON.toJson(new Dtos.RefreshRequest(refresh)), JSON))
                    .build();
            try (okhttp3.Response resp = base.newCall(req).execute()) {
                if (resp.code() >= 400 && resp.code() < 500) return null;
                ResponseBody corpo = resp.body();
                if (!resp.isSuccessful() || corpo == null) {
                    throw new IOException("Falha ao renovar a sessão (erro " + resp.code() + ")");
                }
                Dtos.AuthResponse a = GSON.fromJson(corpo.string(), Dtos.AuthResponse.class);
                if (a == null || a.access_token == null) return null;
                salvarSessao(sessao, a);
                return a.access_token;
            }
        }
    }

    /**
     * Traduz o erro do Supabase para uma mensagem ao aluno. Formatos lidos:
     *  - REST/PostgREST: {"code","message","details","hint"}
     *  - Auth:           {"error","error_description","msg","error_code"}
     *  - API antiga:     {"erro"} (compatibilidade)
     * Sessão expirada (401 / "JWT expired") é tratada no Authenticator: se o
     * refresh falhar, a sessão é marcada como expirada e o app volta ao login.
     */
    public static String mensagemDeErro(Response<?> resposta) {
        Dtos.ErroResponse e = null;
        try (ResponseBody corpo = resposta.errorBody()) {
            if (corpo != null) e = GSON.fromJson(corpo.string(), Dtos.ErroResponse.class);
        } catch (IOException | RuntimeException ignored) {
            // corpo de erro fora do padrão: usa a mensagem genérica abaixo
        }
        return mensagemDeErro(resposta.code(), e);
    }

    /** Versão testável (sem Retrofit): status HTTP + corpo já convertido. */
    public static String mensagemDeErro(int status, Dtos.ErroResponse e) {
        if (e != null) {
            String traduzida = traduzirErroAuth(e);
            if (traduzida != null) return traduzida;
            if (contem(e.message, "JWT expired")) return MSG_SESSAO_EXPIRADA;
            // Regras de negócio das funções SQL (RAISE EXCEPTION) já vêm em português
            if ("P0001".equals(e.code) && e.message != null) return e.message;
            if (e.erro != null && !e.erro.isEmpty()) return e.erro;
        }
        if (status == 401) return MSG_SESSAO_EXPIRADA;
        if (status == 429) return MSG_MUITAS_TENTATIVAS;
        if (status >= 500) return "O servidor está com instabilidade. Tente novamente em alguns minutos.";
        return "Não foi possível concluir (erro " + status + "). Tente novamente.";
    }

    private static boolean contem(String texto, String trecho) {
        return texto != null && texto.toLowerCase(java.util.Locale.ROOT)
                .contains(trecho.toLowerCase(java.util.Locale.ROOT));
    }

    private static String traduzirErroAuth(Dtos.ErroResponse e) {
        String codigo = e.error_code != null ? e.error_code : "";
        String texto = (e.msg != null ? e.msg : "") + " " + (e.error_description != null ? e.error_description : "")
                + " " + (e.error != null ? e.error : "") + " " + (e.message != null ? e.message : "");
        if (codigo.equals("invalid_credentials") || contem(texto, "Invalid login credentials")) {
            return MSG_LOGIN_INVALIDO;
        }
        if (contem(texto, "Email not confirmed")) codigo = "email_not_confirmed";
        if (codigo.equals("user_already_exists") || codigo.equals("email_exists")
                || contem(texto, "User already registered")) {
            return MSG_EMAIL_JA_CADASTRADO;
        }
        switch (codigo) {
            case "email_not_confirmed":
                return "Confirme seu e-mail pelo link que enviamos antes de entrar.";
            case "weak_password":
                return "Essa senha é muito fraca. Use pelo menos 6 caracteres, misturando letras e números.";
            case "same_password":
                return "A nova senha precisa ser diferente da atual.";
            case "email_address_invalid":
                return "Esse e-mail não foi aceito. Confira se ele está correto.";
            case "over_email_send_rate_limit":
            case "over_request_rate_limit":
                return MSG_MUITAS_TENTATIVAS;
            case "signup_disabled":
                return "Os cadastros estão desativados no momento.";
            default:
                return null;
        }
    }

    public static final String MSG_SEM_INTERNET =
            "Sem conexão com a internet. Verifique sua rede e tente de novo.";

    public static final String MSG_LOGIN_INVALIDO = "E-mail ou senha incorretos.";

    public static final String MSG_EMAIL_JA_CADASTRADO = "Este e-mail já tem cadastro.";

    public static final String MSG_SESSAO_EXPIRADA = "Sua sessão expirou. Entre novamente.";

    private static final String MSG_MUITAS_TENTATIVAS =
            "Muitas tentativas seguidas. Aguarde alguns minutos e tente de novo.";
}
