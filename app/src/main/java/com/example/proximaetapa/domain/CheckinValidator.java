package com.example.proximaetapa.domain;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.example.proximaetapa.data.local.entity.Evento;

/**
 * Situação do check-in de um evento, SÓ PARA EXIBIR no ingresso (RFM06).
 *
 * O app não registra presença nem bloqueia nada: quem valida o QR Code é o
 * leitor web da Próxima Etapa (função validar_ingresso no Supabase), que confere
 * inscrição ativa, dia do curso, horário do encontro (±1h) e presença única por dia.
 *
 * Regra da janela do dia D (fuso America/Sao_Paulo), igual à do servidor:
 *  - abre em  D + horário de dataInicio − 1h;
 *  - fecha em D + horário de dataFim + 1h (se o horário de fim ≤ horário de
 *    início, o encontro passa da meia-noite e o fim é no dia seguinte);
 *  - vale para qualquer dia D entre a data de dataInicio e a de dataFim.
 */
public final class CheckinValidator {

    public enum Situacao {
        /** Janela aberta: pode apresentar o QR agora. */
        NO_HORARIO,
        /** Hoje tem encontro, mas o check-in ainda não abriu (ver {@link Resultado#abreAs()}). */
        ANTES_DO_HORARIO,
        /** Hoje teve encontro e o check-in já fechou. */
        ENCERRADO_HOJE,
        /** Hoje não é dia do evento (antes do 1º dia ou depois do último). */
        FORA_DO_PERIODO,
        /** Evento externo (vestibular, ENEM...): não tem check-in da Próxima Etapa. */
        EVENTO_EXTERNO
    }

    /** Janela de check-in de um encontro. */
    public static final class Janela {
        public final ZonedDateTime abre;
        public final ZonedDateTime fecha;

        Janela(ZonedDateTime abre, ZonedDateTime fecha) {
            this.abre = abre;
            this.fecha = fecha;
        }

        boolean contem(ZonedDateTime t) {
            return !t.isBefore(abre) && !t.isAfter(fecha);
        }
    }

    /** Situação + janela relevante (do encontro atual ou do encontro de hoje). */
    public static final class Resultado {
        public final Situacao situacao;
        /** null em FORA_DO_PERIODO e EVENTO_EXTERNO. */
        public final Janela janela;

        Resultado(Situacao situacao, Janela janela) {
            this.situacao = situacao;
            this.janela = janela;
        }

        /** Horário em que o check-in abre (ou abriu) no encontro da janela; null se não houver. */
        public LocalTime abreAs() {
            return janela == null ? null : janela.abre.toLocalTime();
        }

        /** Horário em que o check-in fecha (ou fechou); null se não houver. */
        public LocalTime fechaAs() {
            return janela == null ? null : janela.fecha.toLocalTime();
        }
    }

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final Duration TOLERANCIA = Duration.ofHours(1);

    private final Clock clock;

    public CheckinValidator(Clock clock) {
        this.clock = clock;
    }

    public Resultado avaliar(Evento evento) {
        return avaliar(evento.tipo, evento.dataInicio, evento.dataFim);
    }

    /** Mesma regra, recebendo só os campos necessários (epoch ms). */
    public Resultado avaliar(String tipo, long dataInicio, long dataFim) {
        if (Evento.TIPO_EXTERNO.equals(tipo)) return new Resultado(Situacao.EVENTO_EXTERNO, null);

        ZonedDateTime agora = ZonedDateTime.now(clock).withZoneSameInstant(FUSO);
        ZonedDateTime inicio = Instant.ofEpochMilli(dataInicio).atZone(FUSO);
        ZonedDateTime fim = Instant.ofEpochMilli(dataFim).atZone(FUSO);

        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFim = fim.toLocalTime();
        boolean viraDia = !horaFim.isAfter(horaInicio);

        LocalDate primeiroDia = inicio.toLocalDate();
        LocalDate ultimoDia = fim.toLocalDate();
        // Encontro que passa da meia-noite: o último encontro COMEÇA na véspera de dataFim
        if (viraDia && ultimoDia.isAfter(primeiroDia)) ultimoDia = ultimoDia.minusDays(1);

        // O "agora" pode estar no encontro de ontem (passou da meia-noite), de hoje
        // ou de amanhã (abertura 1h antes de um encontro que começa logo após a meia-noite)
        LocalDate hoje = agora.toLocalDate();
        for (LocalDate d : new LocalDate[]{hoje.minusDays(1), hoje, hoje.plusDays(1)}) {
            if (ehDiaDoEvento(d, primeiroDia, ultimoDia)) {
                Janela j = janela(d, horaInicio, horaFim, viraDia);
                if (j.contem(agora)) return new Resultado(Situacao.NO_HORARIO, j);
            }
        }

        if (ehDiaDoEvento(hoje, primeiroDia, ultimoDia)) {
            Janela j = janela(hoje, horaInicio, horaFim, viraDia);
            return new Resultado(agora.isBefore(j.abre)
                    ? Situacao.ANTES_DO_HORARIO
                    : Situacao.ENCERRADO_HOJE, j);
        }
        return new Resultado(Situacao.FORA_DO_PERIODO, null);
    }

    private static boolean ehDiaDoEvento(LocalDate d, LocalDate primeiro, LocalDate ultimo) {
        return !d.isBefore(primeiro) && !d.isAfter(ultimo);
    }

    private static Janela janela(LocalDate dia, LocalTime horaInicio, LocalTime horaFim, boolean viraDia) {
        ZonedDateTime abre = dia.atTime(horaInicio).atZone(FUSO).minus(TOLERANCIA);
        LocalDate diaFim = viraDia ? dia.plusDays(1) : dia;
        ZonedDateTime fecha = diaFim.atTime(horaFim).atZone(FUSO).plus(TOLERANCIA);
        return new Janela(abre, fecha);
    }
}
