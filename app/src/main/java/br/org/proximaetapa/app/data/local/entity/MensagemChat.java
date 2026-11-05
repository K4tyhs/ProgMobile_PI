package br.org.proximaetapa.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Mensagem do canal de dúvidas com a ProxIA (RFM12). */
@Entity(tableName = "mensagens_chat")
public class MensagemChat {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String texto;
    public boolean doUsuario;
    public long data;

    public static MensagemChat nova(String texto, boolean doUsuario) {
        MensagemChat m = new MensagemChat();
        m.texto = texto;
        m.doUsuario = doUsuario;
        m.data = System.currentTimeMillis();
        return m;
    }
}
