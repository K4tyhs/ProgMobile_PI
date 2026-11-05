package br.org.proximaetapa.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Testes das regras de badges (gamificação). */
public class BadgeCalculatorTest {

    private static Badge buscar(List<Badge> lista, String nome) {
        for (Badge b : lista) if (b.nome.equals(nome)) return b;
        throw new AssertionError("Badge não encontrado: " + nome);
    }

    @Test
    public void niveisDaFecapSeguemQuantidadeDeCursos() {
        Map<String, Integer> total = new HashMap<>();
        total.put("FECAP", 7);
        Map<String, Integer> feitos = new HashMap<>();
        feitos.put("FECAP", 4);

        BadgeCalculator.Entrada e = new BadgeCalculator.Entrada();
        e.totalCursosPorInstituicao = total;
        e.concluidosPorInstituicao = feitos;
        List<Badge> badges = BadgeCalculator.calcular(e);

        assertTrue(buscar(badges, "FECAP Starter Pack").conquistado);
        assertTrue(buscar(badges, "FECAP na Trilha").conquistado);
        assertFalse(buscar(badges, "Alvarista").conquistado);
    }

    @Test
    public void jornadaDoPerfilDefineNivelDoCartao() {
        BadgeCalculator.Entrada e = new BadgeCalculator.Entrada();
        e.perfilCompleto = true;
        e.testesConcluidos = 2;
        e.totalCertificados = 0;
        List<Badge> badges = BadgeCalculator.calcular(e);

        assertEquals("Etapa Bússola", BadgeCalculator.nivelJornada(badges));
        assertFalse(buscar(badges, "Próxima Etapa").conquistado);
    }
}
