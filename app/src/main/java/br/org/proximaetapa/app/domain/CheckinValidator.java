package br.org.proximaetapa.app.domain;

/**
 * Valida o check-in de presença por QR Code (RFM06) ou por código digitado
 * (alternativa acessível quando a câmera não funciona).
 *
 * Formato do QR Code exibido pela organização no evento:
 *     PE:CHECKIN:{eventoId}:{codigo}
 *
 * Regras de negócio (PDP, seção 3):
 *  - a presença é do aluno, no evento, na data -> sem duplicidade no mesmo encontro;
 *  - só quem está inscrito pode registrar presença;
 *  - só vale durante o evento (com 1h de tolerância antes e depois).
 */
public final class CheckinValidator {

    public enum Resultado {
        OK,
        QR_INVALIDO,
        EVENTO_DIFERENTE,
        CODIGO_INVALIDO,
        NAO_INSCRITO,
        FORA_DO_HORARIO,
        JA_REGISTRADO
    }

    public static final String PREFIXO = "PE:CHECKIN:";
    public static final long TOLERANCIA_MS = 60L * 60L * 1000L;

    /** Dados mínimos do evento necessários para a validação. */
    public static final class DadosEvento {
        public final long id;
        public final String codigo;
        public final long inicio;
        public final long fim;

        public DadosEvento(long id, String codigo, long inicio, long fim) {
            this.id = id;
            this.codigo = codigo;
            this.inicio = inicio;
            this.fim = fim;
        }
    }

    private CheckinValidator() {
    }

    public static String gerarConteudoQr(long eventoId, String codigo) {
        return PREFIXO + eventoId + ":" + codigo;
    }

    /** Valida a leitura de um QR Code. */
    public static Resultado validarQr(String conteudo, DadosEvento evento, boolean inscrito,
                                      boolean jaRegistrado, long agora) {
        if (conteudo == null || !conteudo.trim().toUpperCase().startsWith(PREFIXO)) {
            return Resultado.QR_INVALIDO;
        }
        String[] partes = conteudo.trim().substring(PREFIXO.length()).split(":");
        if (partes.length != 2) {
            return Resultado.QR_INVALIDO;
        }
        long idLido;
        try {
            idLido = Long.parseLong(partes[0]);
        } catch (NumberFormatException e) {
            return Resultado.QR_INVALIDO;
        }
        if (idLido != evento.id) {
            return Resultado.EVENTO_DIFERENTE;
        }
        return validarCodigo(partes[1], evento, inscrito, jaRegistrado, agora);
    }

    /** Valida o código do evento (lido do QR ou digitado pelo aluno). */
    public static Resultado validarCodigo(String codigo, DadosEvento evento, boolean inscrito,
                                          boolean jaRegistrado, long agora) {
        if (codigo == null || evento.codigo == null
                || !codigo.trim().equalsIgnoreCase(evento.codigo.trim())) {
            return Resultado.CODIGO_INVALIDO;
        }
        if (!inscrito) {
            return Resultado.NAO_INSCRITO;
        }
        if (agora < evento.inicio - TOLERANCIA_MS || agora > evento.fim + TOLERANCIA_MS) {
            return Resultado.FORA_DO_HORARIO;
        }
        if (jaRegistrado) {
            return Resultado.JA_REGISTRADO;
        }
        return Resultado.OK;
    }

    /** Mensagem clara para o aluno (heurística: reconhecer e corrigir erros). */
    public static String mensagem(Resultado r) {
        switch (r) {
            case OK:
                return "Presença registrada! Bom evento.";
            case QR_INVALIDO:
                return "Esse QR Code não é de um evento da Próxima Etapa. Aponte para o QR Code mostrado pela organização.";
            case EVENTO_DIFERENTE:
                return "Esse QR Code é de outro evento. Confira se você abriu o convite certo.";
            case CODIGO_INVALIDO:
                return "Código do evento incorreto. Confira o código com a organização e tente de novo.";
            case NAO_INSCRITO:
                return "Você não está inscrito neste evento. Faça a inscrição antes de registrar presença.";
            case FORA_DO_HORARIO:
                return "O check-in só fica aberto durante o evento (1h antes até 1h depois).";
            case JA_REGISTRADO:
                return "Sua presença de hoje neste evento já foi registrada.";
            default:
                return "Não foi possível registrar a presença.";
        }
    }
}
