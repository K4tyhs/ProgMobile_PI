package br.org.proximaetapa.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Inscrição do aluno em uma palestra/curso. Gera o convite com o QR Code
 * (ingresso). O id é um UUID criado no celular, o que permite se inscrever
 * mesmo sem internet e sincronizar depois.
 */
@Entity(tableName = "inscricoes",
        indices = {@Index(value = {"alunoId", "eventoId"}, unique = true)})
public class Inscricao {

    public static final String ATIVA = "ATIVA";
    public static final String CANCELADA = "CANCELADA";

    @PrimaryKey
    @NonNull
    public String id = "";

    public long alunoId;
    public long eventoId;
    public long dataInscricao;
    public String status;
    /** Código impresso no ingresso (QR Code do convite). */
    public String codigoIngresso;
    /** false = ainda não foi enviada para a API (fica na fila de sincronização). */
    public boolean sincronizado;
}
