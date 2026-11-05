package com.example.proximaetapa.domain;

import com.example.proximaetapa.data.local.entity.Evento;
import com.example.proximaetapa.data.local.entity.Inscricao;

/**
 * Conteúdo do QR Code do ingresso (RFM06). Formato FIXO, o leitor web depende dele:
 *
 *     PE:INGRESSO:{inscricao.id}:{inscricao.codigoIngresso}
 *
 * Não leva nome, e-mail nem outro dado pessoal (LGPD). O mesmo QR vale para
 * todos os dias do curso; quem decide se a presença vale é o servidor
 * (função validar_ingresso).
 */
public final class IngressoQr {

    public static final String PREFIXO = "PE:INGRESSO:";

    public enum Estado {
        /** Inscrição ATIVA e confirmada pelo servidor: mostra o QR. */
        DISPONIVEL,
        /** Inscrição ATIVA feita offline, ainda não confirmada por rpc/inscrever. */
        SINCRONIZANDO,
        /** Cancelada, evento externo ou dados incompletos: sem QR. */
        INDISPONIVEL
    }

    private IngressoQr() {
    }

    public static Estado estado(String status, boolean sincronizado, boolean eventoExterno,
                                String id, String codigoIngresso) {
        if (eventoExterno || !Inscricao.ATIVA.equals(status) || vazio(id) || vazio(codigoIngresso)) {
            return Estado.INDISPONIVEL;
        }
        return sincronizado ? Estado.DISPONIVEL : Estado.SINCRONIZANDO;
    }

    public static Estado estado(Inscricao inscricao, Evento evento) {
        if (inscricao == null) return Estado.INDISPONIVEL;
        return estado(inscricao.status, inscricao.sincronizado, evento != null && evento.isExterno(),
                inscricao.id, inscricao.codigoIngresso);
    }

    /** Texto do QR, ou null quando o ingresso não pode ser exibido. */
    public static String conteudo(String status, boolean sincronizado, boolean eventoExterno,
                                  String id, String codigoIngresso) {
        if (estado(status, sincronizado, eventoExterno, id, codigoIngresso) != Estado.DISPONIVEL) {
            return null;
        }
        return PREFIXO + id.trim() + ":" + codigoIngresso.trim();
    }

    public static String conteudo(Inscricao inscricao, Evento evento) {
        if (inscricao == null) return null;
        return conteudo(inscricao.status, inscricao.sincronizado, evento != null && evento.isExterno(),
                inscricao.id, inscricao.codigoIngresso);
    }

    private static boolean vazio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
