package com.example.proximaetapa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import com.example.proximaetapa.R;
import com.example.proximaetapa.data.local.entity.Aluno;
import com.example.proximaetapa.data.repository.AppRepository;
import com.example.proximaetapa.ui.main.MainActivity;
import com.example.proximaetapa.ui.widget.CampoSenha;
import com.example.proximaetapa.util.SessionManager;

/**
 * Login do aluno (RFM01). Se já existe sessão salva no aparelho, vai direto
 * para a Home ("entra com a conta uma vez e fica salvo no dispositivo").
 */
public class LoginActivity extends AppCompatActivity {

    /** Mensagem opcional mostrada ao abrir o login (ex.: sessão expirada). */
    public static final String EXTRA_AVISO = "aviso";

    private EditText edtEmail;
    private EditText edtSenha;
    private Button btnEntrar;
    private View progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.get(this).estaLogado()) {
            abrirHome();
            return;
        }

        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edt_email);
        edtSenha = findViewById(R.id.edt_senha);
        btnEntrar = findViewById(R.id.btn_entrar);
        progresso = findViewById(R.id.progresso);
        CampoSenha.configurar(edtSenha, findViewById(R.id.btn_olho_senha));

        btnEntrar.setOnClickListener(v -> entrar());
        edtSenha.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });

        // Intent explícita para a tela de cadastro
        findViewById(R.id.btn_cadastrar).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
        findViewById(R.id.btn_esqueci).setOnClickListener(v -> {
            Intent i = new Intent(this, RecuperarSenhaActivity.class);
            i.putExtra(RecuperarSenhaActivity.EXTRA_EMAIL, texto(edtEmail));
            startActivity(i);
        });

        String aviso = getIntent().getStringExtra(EXTRA_AVISO);
        if (aviso != null) Snackbar.make(btnEntrar, aviso, Snackbar.LENGTH_LONG).show();
    }

    private void entrar() {
        if (!btnEntrar.isEnabled()) return; // já está entrando (evita envio duplo pelo Enter)
        String email = texto(edtEmail);
        String senha = texto(edtSenha);
        edtEmail.setError(null);
        edtSenha.setError(null);

        // Validação antes de chamar a API (mensagens claras - Nielsen)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.erro_email_invalido));
            edtEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            edtSenha.setError(getString(R.string.erro_senha_vazia));
            edtSenha.requestFocus();
            return;
        }

        carregando(true);
        AppRepository.get(this).login(email, senha, new AppRepository.Callback<Aluno>() {
            @Override
            public void sucesso(Aluno aluno) {
                carregando(false);
                abrirHome();
            }

            @Override
            public void erro(String mensagem) {
                carregando(false);
                Snackbar.make(btnEntrar, mensagem, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void carregando(boolean sim) {
        progresso.setVisibility(sim ? View.VISIBLE : View.GONE);
        btnEntrar.setEnabled(!sim);
    }

    private void abrirHome() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    /** Texto do campo sem espaços nas pontas (nunca nulo). */
    public static String texto(EditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }
}
