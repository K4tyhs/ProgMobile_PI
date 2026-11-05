package br.org.proximaetapa.app;

import android.app.Application;

import br.org.proximaetapa.app.util.NotificationHelper;
import br.org.proximaetapa.app.util.Preferencias;

/** Inicialização global do aplicativo. */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Preferencias.aplicarTema(this);
        NotificationHelper.criarCanais(this);
    }
}
