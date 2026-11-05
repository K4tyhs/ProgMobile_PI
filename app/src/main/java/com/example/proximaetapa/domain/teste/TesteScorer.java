package com.example.proximaetapa.domain.teste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula a pontuação de um teste de perfil. Classe pura (testável com JUnit).
 * O resultado tem finalidade de ORIENTAÇÃO educacional/profissional e não é
 * diagnóstico psicológico ou clínico (regra de negócio do PDP).
 */
public final class TesteScorer {

    private TesteScorer() {
    }

    /**
     * @param teste     definição do teste
     * @param respostas índice da alternativa escolhida em cada pergunta (-1 = sem resposta)
     * @return pontuação por dimensão, na ordem em que os perfis foram cadastrados
     */
    public static Map<String, Integer> pontuar(TesteDefinicao teste, int[] respostas) {
        Map<String, Integer> pontos = new LinkedHashMap<>();
        for (String dim : teste.perfis.keySet()) {
            pontos.put(dim, 0);
        }
        for (int i = 0; i < teste.perguntas.size() && i < respostas.length; i++) {
            int escolha = respostas[i];
            List<TesteDefinicao.Alternativa> alts = teste.perguntas.get(i).alternativas;
            if (escolha < 0 || escolha >= alts.size()) continue;
            TesteDefinicao.Alternativa a = alts.get(escolha);
            Integer atual = pontos.get(a.dimensao);
            pontos.put(a.dimensao, (atual == null ? 0 : atual) + a.pontos);
        }
        return pontos;
    }

    /** As N dimensões com maior pontuação (empate: vale a ordem de cadastro). */
    public static List<String> principais(Map<String, Integer> pontos, int n) {
        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(pontos.entrySet());
        // Ordenação estável: em caso de empate mantém a ordem original
        Collections.sort(entradas, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> resultado = new ArrayList<>();
        for (int i = 0; i < entradas.size() && i < n; i++) {
            resultado.add(entradas.get(i).getKey());
        }
        return resultado;
    }

    /** Todas as perguntas foram respondidas? */
    public static boolean completo(int[] respostas) {
        for (int r : respostas) {
            if (r < 0) return false;
        }
        return true;
    }
}
