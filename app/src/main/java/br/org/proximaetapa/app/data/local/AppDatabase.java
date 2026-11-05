package br.org.proximaetapa.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.org.proximaetapa.app.data.local.dao.AlunoDao;
import br.org.proximaetapa.app.data.local.dao.BolsaDao;
import br.org.proximaetapa.app.data.local.dao.CertificadoDao;
import br.org.proximaetapa.app.data.local.dao.EventoDao;
import br.org.proximaetapa.app.data.local.dao.InscricaoDao;
import br.org.proximaetapa.app.data.local.dao.MensagemChatDao;
import br.org.proximaetapa.app.data.local.dao.NotificacaoDao;
import br.org.proximaetapa.app.data.local.dao.PresencaDao;
import br.org.proximaetapa.app.data.local.dao.ResultadoTesteDao;
import br.org.proximaetapa.app.data.local.entity.Aluno;
import br.org.proximaetapa.app.data.local.entity.Bolsa;
import br.org.proximaetapa.app.data.local.entity.Certificado;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.Inscricao;
import br.org.proximaetapa.app.data.local.entity.MensagemChat;
import br.org.proximaetapa.app.data.local.entity.Notificacao;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.data.local.entity.ResultadoTeste;

/**
 * Banco local (SQLite via Room). Guarda uma cópia dos dados do aluno para o
 * app funcionar sem internet e uma fila do que ainda precisa subir para a API.
 */
@Database(
        entities = {
                Aluno.class, Evento.class, Inscricao.class, Presenca.class, Certificado.class,
                Notificacao.class, Bolsa.class, ResultadoTeste.class, MensagemChat.class
        },
        version = 3, // 2 = índices de desempenho; 3 = telefone do aluno
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract AlunoDao alunoDao();

    public abstract EventoDao eventoDao();

    public abstract InscricaoDao inscricaoDao();

    public abstract PresencaDao presencaDao();

    public abstract CertificadoDao certificadoDao();

    public abstract NotificacaoDao notificacaoDao();

    public abstract BolsaDao bolsaDao();

    public abstract ResultadoTesteDao resultadoTesteDao();

    public abstract MensagemChatDao mensagemChatDao();

    public static AppDatabase get(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "proxima_etapa.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instancia;
    }
}
