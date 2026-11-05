package br.org.proximaetapa.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import br.org.proximaetapa.app.data.local.entity.Inscricao;
import br.org.proximaetapa.app.data.local.entity.InscricaoComEvento;

@Dao
public abstract class InscricaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void salvar(Inscricao inscricao);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void salvarSeNaoExistir(List<Inscricao> inscricoes);

    @Query("DELETE FROM inscricoes WHERE alunoId = :alunoId AND sincronizado = 1")
    public abstract void apagarSincronizadas(long alunoId);

    /**
     * Sincronização: substitui as inscrições pelas do servidor, mas mantém as
     * que foram criadas/canceladas offline e ainda não subiram.
     */
    @Transaction
    public void substituirSincronizadas(long alunoId, List<Inscricao> doServidor) {
        apagarSincronizadas(alunoId);
        salvarSeNaoExistir(doServidor);
    }

    @Transaction
    @Query("SELECT * FROM inscricoes WHERE alunoId = :alunoId AND status = 'ATIVA' ORDER BY dataInscricao DESC")
    public abstract LiveData<List<InscricaoComEvento>> observarAtivasComEvento(long alunoId);

    @Query("SELECT * FROM inscricoes WHERE alunoId = :alunoId AND status = 'ATIVA'")
    public abstract LiveData<List<Inscricao>> observarAtivas(long alunoId);

    @Transaction
    @Query("SELECT * FROM inscricoes WHERE id = :id")
    public abstract LiveData<InscricaoComEvento> observarComEvento(String id);

    @Query("SELECT * FROM inscricoes WHERE alunoId = :alunoId AND eventoId = :eventoId LIMIT 1")
    public abstract LiveData<Inscricao> observarPorEvento(long alunoId, long eventoId);

    @Query("SELECT * FROM inscricoes WHERE alunoId = :alunoId AND eventoId = :eventoId LIMIT 1")
    public abstract Inscricao buscarPorEvento(long alunoId, long eventoId);

    @Query("SELECT * FROM inscricoes WHERE id = :id")
    public abstract Inscricao buscar(String id);

    @Query("SELECT eventoId FROM inscricoes WHERE alunoId = :alunoId AND status = 'ATIVA'")
    public abstract List<Long> idsEventosAtivos(long alunoId);

    @Query("SELECT * FROM inscricoes WHERE sincronizado = 0")
    public abstract List<Inscricao> pendentes();

    @Query("UPDATE inscricoes SET sincronizado = 1 WHERE id = :id")
    public abstract void marcarSincronizada(String id);

    @Query("DELETE FROM inscricoes WHERE id = :id")
    public abstract void apagar(String id);
}
