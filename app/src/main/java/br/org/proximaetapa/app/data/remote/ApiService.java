package br.org.proximaetapa.app.data.remote;

import java.util.List;
import java.util.Map;

import br.org.proximaetapa.app.data.local.entity.Aluno;
import br.org.proximaetapa.app.data.local.entity.Bolsa;
import br.org.proximaetapa.app.data.local.entity.Certificado;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.Inscricao;
import br.org.proximaetapa.app.data.local.entity.Notificacao;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.data.local.entity.ResultadoTeste;
import br.org.proximaetapa.app.data.remote.dto.Dtos;
import br.org.proximaetapa.app.domain.teste.TesteDefinicao;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Contrato com o Supabase (Retrofit).
 *  - auth/v1/...       -> Supabase Auth (login, cadastro, senha)
 *  - rest/v1/rpc/...   -> funções SQL do banco (PostgreSQL). Elas usam o
 *    token do aluno logado e devolvem SOMENTE os dados dele (RLS/LGPD).
 * As funções de leitura são chamadas por GET; as que gravam, por POST.
 */
public interface ApiService {

    // ---------- Autenticação (RFM01) ----------
    @POST("auth/v1/token?grant_type=password")
    Call<Dtos.AuthResponse> login(@Body Dtos.LoginRequest corpo);

    @POST("auth/v1/signup")
    Call<Dtos.AuthResponse> cadastrar(@Body Dtos.SignupRequest corpo);

    /** @param redirecionarPara link que o e-mail abre (deep link da tela Nova senha) */
    @POST("auth/v1/recover")
    Call<Void> recuperarSenha(@Query("redirect_to") String redirecionarPara,
                              @Body Dtos.RecuperarSenhaRequest corpo);

    @PUT("auth/v1/user")
    Call<Void> alterarSenha(@Body Dtos.NovaSenhaRequest corpo);

    @POST("auth/v1/logout")
    Call<Void> logout();

    // ---------- Perfil (RFM14) ----------
    @GET("rest/v1/rpc/meu_perfil")
    Call<Aluno> meuPerfil();

    @POST("rest/v1/rpc/atualizar_perfil")
    Call<Aluno> atualizarPerfil(@Body Dtos.Param<Aluno> corpo);

    @POST("rest/v1/rpc/excluir_conta")
    Call<Dtos.MensagemResponse> excluirConta(@Body Map<String, Object> vazio);

    // ---------- Cursos, palestras e eventos (RFM03/RFM05) ----------
    @GET("rest/v1/rpc/listar_eventos")
    Call<List<Evento>> eventos();

    // ---------- Inscrições / ingressos ----------
    @GET("rest/v1/rpc/minhas_inscricoes")
    Call<List<Inscricao>> inscricoes();

    @POST("rest/v1/rpc/inscrever")
    Call<Inscricao> inscrever(@Body Dtos.Param<Inscricao> corpo);

    @POST("rest/v1/rpc/cancelar_inscricao")
    Call<Dtos.MensagemResponse> cancelarInscricao(@Body Dtos.CancelarRequest corpo);

    // ---------- Presença por QR Code (RFM06/RFM07) ----------
    @GET("rest/v1/rpc/minhas_presencas")
    Call<List<Presenca>> presencas();

    @POST("rest/v1/rpc/registrar_presencas")
    Call<List<Presenca>> enviarPresencas(@Body Dtos.Param<List<Presenca>> corpo);

    // ---------- Certificados (RFM09) ----------
    @GET("rest/v1/rpc/meus_certificados")
    Call<List<Certificado>> certificados();

    // ---------- Bolsas ----------
    @GET("rest/v1/rpc/listar_bolsas")
    Call<List<Bolsa>> bolsas();

    // ---------- Mensagens / notificações (RFM04/RFM11/RFM13) ----------
    @GET("rest/v1/rpc/listar_notificacoes")
    Call<List<Notificacao>> notificacoes();

    // ---------- Testes de perfil (RFM10) ----------
    /** Perguntas, alternativas e critérios de classificação cadastrados no banco. */
    @GET("rest/v1/rpc/listar_testes")
    Call<List<TesteDefinicao>> testes();

    @POST("rest/v1/rpc/salvar_resultado_teste")
    Call<ResultadoTeste> enviarResultado(@Body Dtos.Param<ResultadoTeste> corpo);
}
