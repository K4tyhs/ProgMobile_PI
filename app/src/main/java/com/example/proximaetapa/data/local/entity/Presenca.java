package com.example.proximaetapa.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Presença confirmada pelo servidor (somente leitura no app). Quem registra é o
 * leitor web da Próxima Etapa (validar_ingresso); o app só baixa com
 * rpc/minhas_presencas e substitui a tabela local pelo que vier do servidor.
 * O índice único (aluno, evento, data) espelha a regra do servidor.
 */
@Entity(tableName = "presencas",
        indices = {@Index(value = {"alunoId", "eventoId", "dataEncontro"}, unique = true)})
public class Presenca {

    @PrimaryKey
    @NonNull
    public String id = "";

    public long alunoId;
    public long eventoId;
    /** Data do encontro no formato yyyy-MM-dd (fuso America/Sao_Paulo). */
    public String dataEncontro;
    /** Momento exato do check-in no leitor (epoch ms). */
    public long registradoEm;
    /** Origem informada pelo servidor (ex.: "QR"). */
    public String origem;
}
