package br.org.proximaetapa.app.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Calcula os badges do aluno a partir dos dados de participação.
 * Classe Java pura (sem Android) para poder ser testada com JUnit.
 *
 * Regras retiradas do documento "Descrição de Páginas e Fluxo de Arquitetura":
 *  - Jornada: Etapa Zero (entrou no app), Etapa Identidade (completou o perfil),
 *    Etapa Bússola (fez todos os testes de perfil), Próxima Etapa (1º certificado).
 *  - Por faculdade: níveis por quantidade de cursos concluídos.
 */
public final class BadgeCalculator {

    /** Valor especial: o nível exige TODOS os cursos daquela faculdade. */
    static final int TODOS = -1;

    private static final class Regra {
        final String instituicao;
        final int[] limites;
        final String[] nomes;

        Regra(String instituicao, int[] limites, String[] nomes) {
            this.instituicao = instituicao;
            this.limites = limites;
            this.nomes = nomes;
        }
    }

    private static final Regra[] REGRAS = {
            new Regra("FECAP", new int[]{1, 4, TODOS},
                    new String[]{"FECAP Starter Pack", "FECAP na Trilha", "Alvarista"}),
            new Regra("São Camilo", new int[]{1, TODOS},
                    new String[]{"Camilo Starter Pack", "Camilo Explorer"}),
            new Regra("PUC", new int[]{1, TODOS},
                    new String[]{"PUC em Formação", "Puquiano"}),
            new Regra("ESPM", new int[]{1, TODOS},
                    new String[]{"ESPM em Criação", "Mente Criativa"}),
            new Regra("Inteli", new int[]{TODOS}, new String[]{"Arquiteto do Futuro"}),
            new Regra("FGV", new int[]{TODOS}, new String[]{"Estrategista FGV"}),
            new Regra("FAAP", new int[]{TODOS}, new String[]{"Curador de Ideias"}),
            new Regra("Insper", new int[]{TODOS}, new String[]{"Líder de Impacto"})
    };

    /** Dados de entrada para o cálculo. */
    public static final class Entrada {
        public Map<String, Integer> totalCursosPorInstituicao = Collections.emptyMap();
        public Map<String, Integer> concluidosPorInstituicao = Collections.emptyMap();
        public boolean perfilCompleto;
        public int testesConcluidos;
        public int totalTestes = 2;
        public int totalCertificados;
    }

    private BadgeCalculator() {
    }

    public static List<Badge> calcular(Entrada e) {
        List<Badge> lista = new ArrayList<>();

        // Badges de jornada (completar perfil)
        lista.add(new Badge("Etapa Zero", "Entrou no app da Próxima Etapa",
                Badge.GRUPO_JORNADA, true));
        lista.add(new Badge("Etapa Identidade", "Completou o perfil",
                Badge.GRUPO_JORNADA, e.perfilCompleto));
        lista.add(new Badge("Etapa Bússola", "Fez todos os testes de perfil",
                Badge.GRUPO_JORNADA, e.testesConcluidos >= e.totalTestes));
        lista.add(new Badge("Próxima Etapa", "Recebeu o primeiro certificado",
                Badge.GRUPO_JORNADA, e.totalCertificados >= 1));

        // Badges por faculdade
        for (Regra r : REGRAS) {
            int total = valor(e.totalCursosPorInstituicao, r.instituicao);
            int concluidos = valor(e.concluidosPorInstituicao, r.instituicao);
            for (int i = 0; i < r.limites.length; i++) {
                int limite = r.limites[i] == TODOS ? total : r.limites[i];
                boolean ok = limite > 0 && concluidos >= limite;
                String desc = r.limites[i] == TODOS
                        ? "Concluiu todos os cursos da " + r.instituicao
                        : "Concluiu " + limite + (limite == 1 ? " curso" : " cursos") + " da " + r.instituicao;
                lista.add(new Badge(r.nomes[i], desc, r.instituicao, ok));
            }
        }
        return lista;
    }

    /** Nome do nível de jornada mais alto conquistado (mostrado no cartão do aluno). */
    public static String nivelJornada(List<Badge> badges) {
        String nivel = "Etapa Zero";
        for (Badge b : badges) {
            if (Badge.GRUPO_JORNADA.equals(b.grupo) && b.conquistado) {
                nivel = b.nome;
            }
        }
        return nivel;
    }

    public static int contarConquistados(List<Badge> badges) {
        int n = 0;
        for (Badge b : badges) {
            if (b.conquistado) n++;
        }
        return n;
    }

    private static int valor(Map<String, Integer> mapa, String chave) {
        if (mapa == null) return 0;
        Integer v = mapa.get(chave);
        return v == null ? 0 : v;
    }
}
