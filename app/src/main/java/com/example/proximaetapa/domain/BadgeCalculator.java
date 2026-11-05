package com.example.proximaetapa.domain;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Calcula os badges do aluno a partir dos dados de participação.
 * Classe Java pura (sem Android) para poder ser testada com JUnit.
 *
 * Regras retiradas do documento "Descrição de Páginas e Fluxo de Arquitetura":
 *  - Jornada: Etapa Zero (entrou no app), Etapa Identidade (completou o perfil),
 *    Etapa Bússola (fez todos os testes de perfil), Próxima Etapa (1º certificado).
 *  - Por faculdade: níveis por quantidade de cursos CONCLUÍDOS. Curso concluído =
 *    curso com CERTIFICADO emitido (inscrição ou presença sozinha não contam).
 *
 * O total de cursos de cada faculdade vem do documento de fluxo (FECAP 7,
 * São Camilo 3, PUC 3, ESPM 3, demais 1). Se a API listar mais cursos do que
 * isso (a ONG abriu cursos novos), vale o maior número.
 */
public final class BadgeCalculator {

    /** Valor especial: o nível exige TODOS os cursos daquela faculdade. */
    static final int TODOS = -1;

    private static final class Regra {
        final String instituicao;
        /** Chave normalizada usada para reconhecer o nome vindo da API. */
        final String chave;
        /** Quantidade de cursos da faculdade no documento de fluxo. */
        final int totalDocumento;
        final int[] limites;
        final String[] nomes;

        Regra(String instituicao, int totalDocumento, int[] limites, String[] nomes) {
            this.instituicao = instituicao;
            this.chave = normalizar(instituicao);
            this.totalDocumento = totalDocumento;
            this.limites = limites;
            this.nomes = nomes;
        }
    }

    private static final Regra[] REGRAS = {
            new Regra("FECAP", 7, new int[]{1, 4, TODOS},
                    new String[]{"FECAP Starter Pack", "FECAP na Trilha", "Alvarista"}),
            new Regra("São Camilo", 3, new int[]{1, TODOS},
                    new String[]{"Camilo Starter Pack", "Camilo Explorer"}),
            new Regra("PUC", 3, new int[]{1, TODOS},
                    new String[]{"PUC em Formação", "Puquiano"}),
            new Regra("ESPM", 3, new int[]{1, TODOS},
                    new String[]{"ESPM em Criação", "Mente Criativa"}),
            new Regra("Inteli", 1, new int[]{TODOS}, new String[]{"Arquiteto do Futuro"}),
            new Regra("FGV", 1, new int[]{TODOS}, new String[]{"Estrategista FGV"}),
            new Regra("FAAP", 1, new int[]{TODOS}, new String[]{"Curador de Ideias"}),
            new Regra("Insper", 1, new int[]{TODOS}, new String[]{"Líder de Impacto"})
    };

    /** Dados de entrada para o cálculo. */
    public static final class Entrada {
        /** Cursos que a API lista por faculdade (opcional; o mínimo é o do documento). */
        public Map<String, Integer> totalCursosPorInstituicao = Collections.emptyMap();
        /** Certificados emitidos por faculdade (é o que conta como curso concluído). */
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
            int total = Math.max(r.totalDocumento, somar(e.totalCursosPorInstituicao, r.chave));
            int concluidos = somar(e.concluidosPorInstituicao, r.chave);
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

    /** Soma os valores cujas chaves representam a mesma faculdade (ex.: "PUC" e "PUC-SP"). */
    private static int somar(Map<String, Integer> mapa, String chave) {
        if (mapa == null) return 0;
        int soma = 0;
        for (Map.Entry<String, Integer> e : mapa.entrySet()) {
            if (e.getValue() != null && chave.equals(normalizar(e.getKey()))) soma += e.getValue();
        }
        return soma;
    }

    /**
     * Nome da faculdade sem acento, em minúsculas e com apelidos unificados:
     * "São Camilo", "Sao Camilo" e "Centro Universitário São Camilo" -> "sao camilo";
     * "PUC", "PUC-SP" -> "puc".
     */
    static String normalizar(String nome) {
        if (nome == null) return "";
        String s = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        if (s.contains("camilo")) return "sao camilo";
        if (s.startsWith("puc")) return "puc";
        return s;
    }
}
