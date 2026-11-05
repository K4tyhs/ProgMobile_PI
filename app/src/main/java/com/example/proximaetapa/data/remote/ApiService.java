package com.example.proximaetapa.data.remote;

import java.util.List;
import java.util.Map;

import com.example.proximaetapa.data.local.entity.Aluno;
import com.example.proximaetapa.data.local.entity.Bolsa;
import com.example.proximaetapa.data.local.entity.Certificado;
import com.example.proximaetapa.data.local.entity.Evento;
import com.example.proximaetapa.data.local.entity.Inscricao;
import com.example.proximaetapa.data.local.entity.Notificacao;
import com.example.proximaetapa.data.local.entity.Presenca;
import com.example.proximaetapa.data.local.entity.ResultadoTeste;
import com.example.proximaetapa.data.remote.dto.Dtos;
import com.example.proximaetapa.domain.teste.TesteDefinicao;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Contrato com o Supabase (Retrofit).
 *  - auth/v1/...      -> Supabase Auth (login, cadastro, senha).
 *  - rest/v1/rpc/...  -> funções SQL do banco, sempre por POST. Usam o token do
 *    aluno logado e devolvem SOMENTE os dados dele (RLS/LGPD). Funções sem
 *    parâmetro recebem o corpo vazio {@link Dtos#VAZIO} ("{}").
 *
 * O app NÃO registra presença: o check-in é feito por um leitor web externo
 * (função validar_ingresso). O app só lê as presenças (minhas_presencas).
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
    @POST("rest/v1/rpc/meu_perfil")
    Call<Aluno> meuPerfil(@Body Map<String, Object> vazio);

    @POST("rest/v1/rpc/atualizar_perfil")
    Call<Aluno> atualizarPerfil(@Body Dtos.Param<Aluno> corpo);

    @POST("rest/v1/rpc/excluir_conta")
    Call<Dtos.MensagemResponse> excluirConta(@Body Map<String, Object> vazio);

    // ---------- Cursos, palestras e eventos (RFM03/RFM05) ----------
    @POST("rest/v1/rpc/listar_eventos")
    Call<List<Evento>> eventos(@Body Map<String, Object> vazio);

    // ---------- Inscrições / ingressos ----------
    @POST("rest/v1/rpc/minhas_inscricoes")
    Call<List<Inscricao>> inscricoes(@Body Map<String, Object> vazio);

    @POST("rest/v1/rpc/inscrever")
    Call<Inscricao> inscrever(@Body Dtos.Param<Dtos.InscricaoRequest> corpo);

    @POST("rest/v1/rpc/cancelar_inscricao")
    Call<Dtos.MensagemResponse> cancelarInscricao(@Body Dtos.CancelarRequest corpo);

    // ---------- Presença (RFM07) - somente leitura ----------
    @POST("rest/v1/rpc/minhas_presencas")
    Call<List<Presenca>> presencas(@Body Map<String, Object> vazio);

    // ---------- Certificados (RFM09) ----------
    @POST("rest/v1/rpc/meus_certificados")
    Call<List<Certificado>> certificados(@Body Map<String, Object> vazio);

    // ---------- Bolsas ----------
    @POST("rest/v1/rpc/listar_bolsas")
    Call<List<Bolsa>> bolsas(@Body Map<String, Object> vazio);

    // ---------- Mensagens / notificações (RFM04/RFM11/RFM13) ----------
    @POST("rest/v1/rpc/listar_notificacoes")
    Call<List<Notificacao>> notificacoes(@Body Map<String, Object> vazio);

    // ---------- Testes de perfil (RFM10) ----------
    /** Perguntas, alternativas e critérios de classificação cadastrados no banco. */
    @POST("rest/v1/rpc/listar_testes")
    Call<List<TesteDefinicao>> testes(@Body Map<String, Object> vazio);

    @POST("rest/v1/rpc/salvar_resultado_teste")
    Call<ResultadoTeste> enviarResultado(@Body Dtos.Param<ResultadoTeste> corpo);
}
