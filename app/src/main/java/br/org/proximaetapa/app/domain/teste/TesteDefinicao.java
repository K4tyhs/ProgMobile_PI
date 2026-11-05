package br.org.proximaetapa.app.domain.teste;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Definição de um teste de perfil (perguntas, alternativas e critérios de
 * classificação). É lida do JSON (assets/testes.json hoje; da API depois),
 * conforme o PDP: "perguntas, alternativas, critérios de pontuação e resultado".
 */
public class TesteDefinicao {

    public String id;            // "DISC" ou "RIASEC"
    public String titulo;
    public String subtitulo;
    public int duracaoMinutos;
    public String instrucao;
    public List<Pergunta> perguntas = new ArrayList<>();
    /** Chave = dimensão (ex.: "D", "I", "S", "C"). A ordem do JSON é preservada. */
    public Map<String, PerfilResultado> perfis = new LinkedHashMap<>();

    public static class Pergunta {
        public String texto;
        public List<Alternativa> alternativas = new ArrayList<>();
    }

    public static class Alternativa {
        public String texto;
        public String dimensao;
        public int pontos;
    }

    public static class PerfilResultado {
        public String nome;
        public String descricao;
        public List<String> sugestoes = new ArrayList<>();
    }
}
