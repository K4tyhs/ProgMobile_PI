package com.example.proximaetapa.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Formatação de datas em português do Brasil. */
public final class DateUtils {

    public static final Locale PT_BR = new Locale("pt", "BR");

    private DateUtils() {
    }

    private static String formatar(String padrao, long millis) {
        return new SimpleDateFormat(padrao, PT_BR).format(new Date(millis));
    }

    /** yyyy-MM-dd (usado como chave do encontro na presença). */
    public static String dataIso(long millis) {
        return formatar("yyyy-MM-dd", millis);
    }

    public static String hojeIso() {
        return dataIso(System.currentTimeMillis());
    }

    /** "2026-11-03" (dataEncontro da API) -> "03/11/2026". Devolve o texto original se não reconhecer. */
    public static String dataIsoParaBr(String iso) {
        if (iso == null) return "";
        String[] p = iso.split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }

    public static String data(long millis) {
        return formatar("dd/MM/yyyy", millis);
    }

    public static String hora(long millis) {
        return formatar("HH:mm", millis);
    }

    public static String dataHora(long millis) {
        return formatar("dd/MM/yyyy 'às' HH:mm", millis);
    }

    /** Ex.: "qua, 12 de nov" */
    public static String diaSemanaCurto(long millis) {
        return formatar("EEE, d 'de' MMM", millis);
    }

    /** Ex.: "Maio" */
    public static String nomeMes(long millis) {
        String m = formatar("MMMM", millis);
        return m.substring(0, 1).toUpperCase(PT_BR) + m.substring(1);
    }

    public static String diaDoMes(long millis) {
        return formatar("d", millis);
    }

    /** Ex.: "14:00 às 17:30" */
    public static String intervaloHoras(long inicio, long fim) {
        return hora(inicio) + " às " + hora(fim);
    }

    /** Período legível de um evento (mesmo dia ou vários dias). */
    public static String periodo(long inicio, long fim) {
        if (dataIso(inicio).equals(dataIso(fim))) {
            return data(inicio) + " · " + intervaloHoras(inicio, fim);
        }
        return data(inicio) + " a " + data(fim) + " · " + intervaloHoras(inicio, fim);
    }

    public static Calendar inicioDoDia(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** Segunda-feira da semana da data informada. */
    public static Calendar inicioDaSemana(long millis) {
        Calendar c = inicioDoDia(millis);
        int dia = c.get(Calendar.DAY_OF_WEEK); // domingo = 1
        int voltar = (dia == Calendar.SUNDAY) ? 6 : dia - Calendar.MONDAY;
        c.add(Calendar.DAY_OF_MONTH, -voltar);
        return c;
    }

    /** Ex.: "03-09 nov" */
    public static String rotuloSemana(Calendar segunda) {
        Calendar domingo = (Calendar) segunda.clone();
        domingo.add(Calendar.DAY_OF_MONTH, 6);
        String ini = formatar("dd", segunda.getTimeInMillis());
        String fim = formatar("dd MMM", domingo.getTimeInMillis());
        if (segunda.get(Calendar.MONTH) != domingo.get(Calendar.MONTH)) {
            ini = formatar("dd MMM", segunda.getTimeInMillis());
        }
        return ini + " - " + fim;
    }

    public static boolean mesmoDia(long a, long b) {
        return dataIso(a).equals(dataIso(b));
    }

    /** O dia (00:00 a 23:59) toca o intervalo [inicio, fim] do evento? */
    public static boolean diaDentroDoEvento(long diaMeiaNoite, long inicio, long fim) {
        long fimDoDia = diaMeiaNoite + 24L * 60 * 60 * 1000 - 1;
        return inicio <= fimDoDia && fim >= diaMeiaNoite;
    }
}
