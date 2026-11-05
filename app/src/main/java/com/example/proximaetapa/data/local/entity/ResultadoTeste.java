package com.example.proximaetapa.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Resultado de um teste de perfil (DISC ou RIASEC) feito pelo aluno. */
@Entity(tableName = "resultados_teste", indices = {@Index("alunoId")})
public class ResultadoTeste {

    @PrimaryKey
    @NonNull
    public String id = "";

    public long alunoId;
    /** "DISC" ou "RIASEC". */
    public String tipo;
    /** Dimensão principal (ex.: "I") ou códigos (ex.: "RI"). */
    public String codigo;
    /** Nome amigável do perfil (ex.: "Influente"). */
    public String perfil;
    /** Pontuação por dimensão em JSON, ex.: {"D":6,"I":12,...}. */
    public String pontuacoesJson;
    public long data;
    public boolean sincronizado;
}
