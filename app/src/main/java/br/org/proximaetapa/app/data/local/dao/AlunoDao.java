package br.org.proximaetapa.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.org.proximaetapa.app.data.local.entity.Aluno;

@Dao
public interface AlunoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvar(Aluno aluno);

    @Query("SELECT * FROM alunos WHERE id = :id")
    LiveData<Aluno> observar(long id);

    @Query("SELECT * FROM alunos WHERE id = :id")
    Aluno buscar(long id);
}
