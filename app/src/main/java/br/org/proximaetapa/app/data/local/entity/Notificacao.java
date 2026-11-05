package br.org.proximaetapa.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Mensagens e comunicados da Próxima Etapa (RFM11/RFM13).
 * Os campos "lida", "removida" e "exibidaNoSistema" são só locais
 * (o servidor não os envia; preservamos na sincronização).
 */
@Entity(tableName = "notificacoes", indices = {@Index("dataPublicacao")})
public class Notificacao {

    public static final String TIPO_PALESTRA = "PALESTRA";
    public static final String TIPO_EXTERNO = "EXTERNO";
    public static final String TIPO_CURSO = "CURSO";
    public static final String TIPO_CERTIFICADO = "CERTIFICADO";
    public static final String TIPO_AVISO = "AVISO";
    /** Gerada no próprio app quando muda data/horário/local de um evento inscrito. */
    public static final String TIPO_AGENDA = "AGENDA";

    @PrimaryKey
    public long id;

    public String titulo;
    public String mensagem;
    public String tipo;
    /** Evento relacionado (pode ser nulo). */
    public Long eventoId;
    public long dataPublicacao;
    /** Depois dessa data o card some sozinho (um dia após o evento). */
    public Long expiraEm;

    public boolean lida;
    public boolean removida;
    public boolean exibidaNoSistema;
}
