package br.org.proximaetapa.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.Aluno;
import br.org.proximaetapa.app.data.remote.dto.Dtos;
import br.org.proximaetapa.app.data.repository.AppRepository;
import br.org.proximaetapa.app.ui.perfil.EditarPerfilActivity;
import br.org.proximaetapa.app.ui.widget.CampoSenha;

/**
 * Cadastro do primeiro acesso (Figma: nome, e-mail, telefone, senha e confirmação).
 * O telefone é um dado demográfico: fica em Editar perfil e não aparece no
 * cartão do aluno. Depois do cadastro abre o onboarding opcional de interesses.
 */
public class CadastroActivity extends AppCompatActivity {

    private static final int SENHA_MINIMA = 6;

    private EditText edtNome, edtEmail, edtTelefone, edtSenha, edtConfirmacao;
    private Button btnCadastrar;
    private View progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        edtNome = findViewById(R.id.edt_nome);
        edtEmail = findViewById(R.id.edt_email);
        edtTelefone = findViewById(R.id.edt_telefone);
        edtSenha = findViewById(R.id.edt_senha);
        edtConfirmacao = findViewById(R.id.edt_confirmacao);
        btnCadastrar = findViewById(R.id.btn_cadastrar);
        progresso = findViewById(R.id.progresso);

        CampoSenha.configurar(edtSenha, findViewById(R.id.btn_olho_senha));
        CampoSenha.configurar(edtConfirmacao, findViewById(R.id.btn_olho_confirmacao));

        btnCadastrar.setOnClickListener(v -> cadastrar());
        findViewById(R.id.btn_ja_tenho).setOnClickListener(v -> finish());
    }

    private void cadastrar() {
        String nome = LoginActivity.texto(edtNome);
        String email = LoginActivity.texto(edtEmail).toLowerCase();
        String telefone = LoginActivity.texto(edtTelefone);
        int digitosTelefone = telefone.replaceAll("\\D", "").length();
        String senha = LoginActivity.texto(edtSenha);
        String confirmacao = LoginActivity.texto(edtConfirmacao);

        // Validações com mensagens claras (prevenção de erros - Nielsen).
        // Valida de cima para baixo e leva o foco ao primeiro campo com problema.
        if (nome.split("\\s+").length < 2) {
            erro(edtNome, R.string.erro_nome);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            erro(edtEmail, R.string.erro_email_invalido);
            return;
        }
        // Telefone é opcional; se preenchido, precisa ter DDD + número (10 ou 11 dígitos)
        if (!telefone.isEmpty() && (digitosTelefone < 10 || digitosTelefone > 11)) {
            erro(edtTelefone, R.string.erro_telefone);
            return;
        }
        if (senha.length() < SENHA_MINIMA) {
            erro(edtSenha, R.string.erro_senha_curta);
            return;
        }
        if (!senha.equals(confirmacao)) {
            erro(edtConfirmacao, R.string.erro_senhas_diferentes);
            return;
        }

        Dtos.CadastroRequest req = new Dtos.CadastroRequest();
        req.nome = nome;
        req.email = email;
        req.telefone = telefone;
        req.senha = senha;

        progresso.setVisibility(View.VISIBLE);
        btnCadastrar.setEnabled(false);
        AppRepository.get(this).cadastrar(req, new AppRepository.Callback<Aluno>() {
            @Override
            public void sucesso(Aluno aluno) {
                Intent i = new Intent(CadastroActivity.this, EditarPerfilActivity.class);
                i.putExtra(EditarPerfilActivity.EXTRA_ONBOARDING, true);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void erro(String mensagem) {
                progresso.setVisibility(View.GONE);
                btnCadastrar.setEnabled(true);
                Snackbar.make(btnCadastrar, mensagem, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void erro(EditText campo, int mensagem) {
        campo.setError(getString(mensagem));
        campo.requestFocus();
    }
}
