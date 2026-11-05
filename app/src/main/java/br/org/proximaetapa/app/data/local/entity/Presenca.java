package br.org.proximaetapa.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registro de presença (check-in). O índice único (aluno, evento, data)
 * impede presença duplicada no mesmo encontro — regra do PDP.
 */
@Entity(tableName = "presencas",
        indices = {@Index(value = {"alunoId", "eventoId", "dataEncontro"}, unique = true)})
public class Presenca {

    public static final String ORIGEM_QR = "QR";
    public static final String ORIGEM_CODIGO = "CODIGO";

    @PrimaryKey
    @NonNull
    public String id = "";

    public long alunoId;
    public long eventoId;
    /** Data do encontro no formato yyyy-MM-dd. */
    public String dataEncontro;
    /** Momento exato do check-in (epoch ms). */
    public long registradoEm;
    public String origem;
    public boolean sincronizado;
}
