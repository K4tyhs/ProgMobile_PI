package br.org.proximaetapa.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import br.org.proximaetapa.app.data.local.entity.Presenca;

@Dao
public abstract class PresencaDao {

    /** ABORT: se já existir presença no mesmo encontro, o índice único barra. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract void inserir(Presenca presenca);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void salvarSeNaoExistir(List<Presenca> presencas);

    @Query("DELETE FROM presencas WHERE alunoId = :alunoId AND sincronizado = 1")
    public abstract void apagarSincronizadas(long alunoId);

    @Transaction
    public void substituirSincronizadas(long alunoId, List<Presenca> doServidor) {
        apagarSincronizadas(alunoId);
        salvarSeNaoExistir(doServidor);
    }

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId ORDER BY registradoEm DESC")
    public abstract LiveData<List<Presenca>> observarTodas(long alunoId);

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId AND eventoId = :eventoId ORDER BY registradoEm DESC")
    public abstract LiveData<List<Presenca>> observarPorEvento(long alunoId, long eventoId);

    @Query("SELECT * FROM presencas WHERE alunoId = :alunoId AND eventoId = :eventoId AND dataEncontro = :data LIMIT 1")
    public abstract Presenca buscar(long alunoId, long eventoId, String data);

    @Query("SELECT * FROM presencas WHERE sincronizado = 0")
    public abstract List<Presenca> pendentes();

    @Query("UPDATE presencas SET sincronizado = 1 WHERE id = :id")
    public abstract void marcarSincronizada(String id);
}
