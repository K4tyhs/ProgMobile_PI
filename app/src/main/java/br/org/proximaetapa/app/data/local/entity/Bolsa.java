package br.org.proximaetapa.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Bolsa de estudo nacional ou internacional (funcionalidade mais pedida: 77,8%). */
@Entity(tableName = "bolsas")
public class Bolsa {

    @PrimaryKey
    public long id;

    public String nome;
    public String instituicao;
    public String descricao;
    public String requisitos;
    public String duracao;
    public String prazo;
    public String link;
    public boolean internacional;
    public boolean destaque;
}
