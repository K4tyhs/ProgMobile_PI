package br.org.proximaetapa.app.ui.testes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.ResultadoTeste;
import br.org.proximaetapa.app.data.repository.AppRepository;
import br.org.proximaetapa.app.data.repository.TesteRepository;
import br.org.proximaetapa.app.domain.teste.TesteDefinicao;
import br.org.proximaetapa.app.ui.BaseActivity;

/** Lista dos testes de perfil (DISC e vocacional RIASEC) com o último resultado. */
public class TestesActivity extends BaseActivity {

    private final TesteAdapter adapter = new TesteAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testes);
        configurarToolbar(R.string.menu_testes);

        adapter.testes = TesteRepository.todos(this);
        ((RecyclerView) findViewById(R.id.rv_testes)).setAdapter(adapter);

        AppRepository.get(this).resultadosTeste().observe(this, lista -> {
            // Último resultado de cada tipo (a lista vem do mais recente para o mais antigo)
            Map<String, ResultadoTeste> ultimo = new HashMap<>();
            if (lista != null) {
                for (ResultadoTeste r : lista) {
                    if (!ultimo.containsKey(r.tipo)) ultimo.put(r.tipo, r);
                }
            }
            adapter.ultimo = ultimo;
            adapter.notifyDataSetChanged();
        });
    }

    private void abrirQuestionario(TesteDefinicao t) {
        Intent i = new Intent(this, QuestionarioActivity.class);
        i.putExtra(QuestionarioActivity.EXTRA_TESTE_ID, t.id);
        startActivity(i);
    }

    private void abrirResultado(ResultadoTeste r) {
        Intent i = new Intent(this, ResultadoTesteActivity.class);
        i.putExtra(ResultadoTesteActivity.EXTRA_RESULTADO_ID, r.id);
        startActivity(i);
    }

    /** Cards dos testes (RecyclerView). */
    private class TesteAdapter extends RecyclerView.Adapter<TesteAdapter.VH> {

        List<TesteDefinicao> testes;
        Map<String, ResultadoTeste> ultimo = new HashMap<>();

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teste, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            TesteDefinicao t = testes.get(position);
            boolean primeiro = position == 0;
            h.icone.setImageResource(primeiro ? R.drawable.ic_pessoa : R.drawable.ic_ingresso);
            h.icone.setBackgroundResource(primeiro ? R.drawable.bg_icone_teste_azul : R.drawable.bg_icone_teste_verde);
            h.titulo.setText(t.titulo);
            h.sub.setText(getString(R.string.teste_sub, t.subtitulo, t.duracaoMinutos));

            ResultadoTeste r = ultimo.get(t.id);
            if (r != null) {
                h.resultado.setVisibility(View.VISIBLE);
                h.resultado.setText(getString(R.string.seu_resultado, r.perfil));
                h.iniciar.setText(R.string.refazer_teste);
                h.verResultado.setVisibility(View.VISIBLE);
                h.verResultado.setOnClickListener(v -> abrirResultado(r));
            } else {
                h.resultado.setVisibility(View.GONE);
                h.iniciar.setText(R.string.iniciar_teste);
                h.verResultado.setVisibility(View.GONE);
            }
            h.iniciar.setOnClickListener(v -> abrirQuestionario(t));
        }

        @Override
        public int getItemCount() {
            return testes == null ? 0 : testes.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView icone;
            final TextView titulo, sub, resultado;
            final MaterialButton iniciar, verResultado;

            VH(View v) {
                super(v);
                icone = v.findViewById(R.id.img_icone);
                titulo = v.findViewById(R.id.txt_titulo);
                sub = v.findViewById(R.id.txt_sub);
                resultado = v.findViewById(R.id.txt_resultado);
                iniciar = v.findViewById(R.id.btn_iniciar);
                verResultado = v.findViewById(R.id.btn_resultado);
            }
        }
    }
}
