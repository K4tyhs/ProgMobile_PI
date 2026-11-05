package com.example.proximaetapa.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import com.example.proximaetapa.data.local.entity.Presenca;

/** Presenças do aluno: cópia local do que o servidor devolve (somente leitura). */
@Dao
public abstract class PresencaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void salvarTodas(List<Presenca> presencas);

    @Query("DELETE FROM presencas WHERE alunoId = :alunoId")
    public abstract void apagarDoAluno(long alunoId);

    /** O servidor é a única fonte de verdade: troca tudo pelo que veio dele. */
    @Transaction
    public void substituirTodas(long alunoId, List<Presenca> doServidor) {
        apagarDoAluno(alunoId);
        salvarTodas(doServidor);
    }

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId")
    public abstract List<Presenca> listar(long alunoId);

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId ORDER BY registradoEm DESC")
    public abstract LiveData<List<Presenca>> observarTodas(long alunoId);

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId AND eventoId = :eventoId ORDER BY registradoEm DESC")
    public abstract LiveData<List<Presenca>> observarPorEvento(long alunoId, long eventoId);
}
