package br.org.proximaetapa.app.domain;

/**
 * Conquista (badge) do aluno. Os badges servem para engajar o estudante e,
 * na página web administrativa, para elencar os alunos mais ativos.
 */
public class Badge {

    /** Grupo "jornada" = badges de completar o perfil; demais = nome da faculdade. */
    public static final String GRUPO_JORNADA = "Jornada";

    public final String nome;
    public final String descricao;
    public final String grupo;
    public final boolean conquistado;

    public Badge(String nome, String descricao, String grupo, boolean conquistado) {
        this.nome = nome;
        this.descricao = descricao;
        this.grupo = grupo;
        this.conquistado = conquistado;
    }
}
