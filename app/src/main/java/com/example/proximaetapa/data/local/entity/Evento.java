package com.example.proximaetapa.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Curso, palestra (eventos internos da Próxima Etapa) ou evento externo
 * (vestibular, inscrição do ENEM...). Datas em milissegundos (epoch).
 */
/* Índice em dataInicio: listas e agenda ordenam/filtram por data */
@Entity(tableName = "eventos", indices = {@Index("dataInicio")})
public class Evento {

    public static final String TIPO_CURSO = "CURSO";
    public static final String TIPO_PALESTRA = "PALESTRA";
    public static final String TIPO_EXTERNO = "EXTERNO";

    @PrimaryKey
    public long id;

    public String titulo;
    public String descricao;
    public String instituicao;
    public String local;
    public String tipo;
    public String categoria;
    public long dataInicio;
    public long dataFim;
    /** Carga horária total em horas (dado numérico do PDP). */
    public int cargaHoraria;
    /** Quantidade de encontros (cada dia do evento é um encontro). */
    public int totalEncontros;
    public String linkExterno;
    /** Ex.: Soft skills só aceita inscrição de quem fez os testes de perfil. */
    public boolean requerTestes;

    public boolean isExterno() {
        return TIPO_EXTERNO.equals(tipo);
    }

    public boolean isCurso() {
        return TIPO_CURSO.equals(tipo);
    }

    public boolean jaTerminou(long agora) {
        return dataFim < agora;
    }
}
