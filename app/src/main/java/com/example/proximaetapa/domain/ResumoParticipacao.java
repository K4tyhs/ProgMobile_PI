package com.example.proximaetapa.domain;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Resumo da participação do aluno em UM curso/palestra (RFM07):
 * presenças, faltas, encontros que ainda vão acontecer e percentual.
 *
 * Regra: só vira falta o encontro que já terminou (até ontem) sem presença.
 * O encontro de hoje ainda não conta como falta, pois o aluno pode registrar.
 */
public final class ResumoParticipacao {

    public final int totalEncontros;
    public final int presencas;
    public final int faltas;
    public final int futuros;
    /** Presenças sobre os encontros que já aconteceram (0 a 100). */
    public final int percentual;
    public final boolean elegivelCertificado;
    public final int faltamParaCertificado;

    private ResumoParticipacao(int total, int presencas, int faltas, int futuros) {
        this.totalEncontros = total;
        this.presencas = presencas;
        this.faltas = faltas;
        this.futuros = futuros;
        this.percentual = ParticipacaoCalculator.percentual(presencas, presencas + faltas);
        this.elegivelCertificado = ParticipacaoCalculator.elegivelCertificado(presencas, total);
        this.faltamParaCertificado = ParticipacaoCalculator.presencasFaltantes(presencas, total);
    }

    public static ResumoParticipacao calcular(long inicio, long fim, int totalEncontros,
                                              int presencas, long agora, TimeZone fuso) {
        int total = Math.max(0, totalEncontros);
        int pres = Math.max(0, Math.min(presencas, total));
        long fimDeOntem = inicioDoDia(agora, fuso) - 1;
        int ocorridosAteOntem = ParticipacaoCalculator.encontrosOcorridos(inicio, fim, total, fimDeOntem, fuso);
        int faltas = Math.max(0, ocorridosAteOntem - pres);
        int futuros = Math.max(0, total - pres - faltas);
        return new ResumoParticipacao(total, pres, faltas, futuros);
    }

    private static long inicioDoDia(long millis, TimeZone fuso) {
        Calendar c = Calendar.getInstance(fuso);
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
