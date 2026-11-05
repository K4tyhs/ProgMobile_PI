package com.example.proximaetapa.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Certificado emitido pela Próxima Etapa quando o aluno cumpre os critérios. */
@Entity(tableName = "certificados", indices = {@Index("alunoId")})
public class Certificado {

    @PrimaryKey
    public long id;

    public long alunoId;
    public long eventoId;
    public String titulo;
    public String instituicao;
    public int cargaHoraria;
    public long dataEmissao;
    /** Código para conferir a autenticidade do certificado. */
    public String codigoValidacao;
}
