package com.example.proximaetapa.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

/** Inscrição junto com os dados do evento (consulta com @Relation do Room). */
public class InscricaoComEvento {

    @Embedded
    public Inscricao inscricao;

    @Relation(parentColumn = "eventoId", entityColumn = "id")
    public Evento evento;
}
