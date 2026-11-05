package br.org.proximaetapa.app.ui;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import br.org.proximaetapa.app.R;

/** Base das telas secundárias: configura a barra superior com "voltar". */
public abstract class BaseActivity extends AppCompatActivity {

    protected void configurarToolbar(CharSequence titulo) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) return;
        toolbar.setTitle(titulo);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    protected void configurarToolbar(int tituloRes) {
        configurarToolbar(getString(tituloRes));
    }
}
