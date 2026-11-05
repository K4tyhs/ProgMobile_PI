package com.example.proximaetapa.data.remote.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Objetos de requisição/resposta do Supabase (convertidos de/para JSON pelo Gson).
 *  - Auth (GoTrue): login, cadastro, renovação do token, senha.
 *  - Funções RPC do banco (PostgREST): recebem os dados no parâmetro "p".
 */
public final class Dtos {

    private Dtos() {
    }

    /** Corpo vazio "{}" para funções RPC sem parâmetros chamadas por POST. */
    public static final Map<String, Object> VAZIO = Collections.emptyMap();

    // ---------------- Auth ----------------

    /** POST auth/v1/token?grant_type=password */
    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String senha) {
            this.email = email;
            this.password = senha;
        }
    }

    /** POST auth/v1/token?grant_type=refresh_token */
    public static class RefreshRequest {
        public String refresh_token;

        public RefreshRequest(String refreshToken) {
            this.refresh_token = refreshToken;
        }
    }

    /** Dados preenchidos na tela de cadastro. */
    public static class CadastroRequest {
        public String nome;
        public String email;
        public String telefone;
        public String endereco;
        public String cidade;
        public String escola;
        public String senha;
    }

    /**
     * POST auth/v1/signup. Os campos de "data" vão para o user_metadata e o
     * gatilho do banco usa esses dados para criar o registro em "alunos".
     */
    public static class SignupRequest {
        public String email;
        public String password;
        public Map<String, String> data = new HashMap<>();

        public SignupRequest(CadastroRequest c) {
            email = c.email;
            password = c.senha;
            data.put("nome", c.nome);
            data.put("full_name", c.nome); // usado pelo perfil do site
            data.put("telefone", c.telefone);
            data.put("endereco", c.endereco);
            data.put("cidade", c.cidade);
            data.put("escola", c.escola);
        }
    }

    /** Sessão devolvida pelo Auth (login, cadastro e renovação). */
    public static class AuthResponse {
        public String access_token;
        public String refresh_token;
        public long expires_in;
        /** Momento em que o token expira (segundos desde 1970). */
        public long expires_at;
    }

    public static class RecuperarSenhaRequest {
        public String email;

        public RecuperarSenhaRequest(String email) {
            this.email = email;
        }
    }

    /** PUT auth/v1/user */
    public static class NovaSenhaRequest {
        public String password;

        public NovaSenhaRequest(String novaSenha) {
            this.password = novaSenha;
        }
    }

    // ---------------- Funções RPC ----------------

    /** Envolve o corpo no parâmetro "p" das funções RPC: {"p": ...}. */
    public static class Param<T> {
        public T p;

        public Param(T valor) {
            this.p = valor;
        }
    }

    /**
     * Corpo de rpc/inscrever: {"p": {id, eventoId, dataInscricao, codigoIngresso}}.
     * O aluno vem do token (nunca enviado pelo app); o status é definido pelo servidor.
     */
    public static class InscricaoRequest {
        public String id;
        public long eventoId;
        public long dataInscricao;
        public String codigoIngresso;

        public InscricaoRequest(String id, long eventoId, long dataInscricao, String codigoIngresso) {
            this.id = id;
            this.eventoId = eventoId;
            this.dataInscricao = dataInscricao;
            this.codigoIngresso = codigoIngresso;
        }
    }

    public static class CancelarRequest {
        public String p_id;

        public CancelarRequest(String id) {
            this.p_id = id;
        }
    }

    public static class MensagemResponse {
        public String mensagem;
    }

    /**
     * Erro devolvido pelo Supabase. O PostgREST usa "message"/"code"
     * e o Auth usa "msg"/"error_description"/"error_code".
     */
    public static class ErroResponse {
        // PostgREST (rest/v1): {"code","message","details","hint"}
        public String message;
        public String code;
        public String details;
        public String hint;
        // Auth (auth/v1): {"error","error_description","msg","error_code"}
        public String error;
        public String msg;
        public String error_description;
        public String error_code;
        // Contrato antigo da API REST do grupo: {"erro"}
        public String erro;
    }
}
