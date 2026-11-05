package br.org.proximaetapa.app.ui.ingressos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.Certificado;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.InscricaoComEvento;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.data.repository.AppRepository;
import br.org.proximaetapa.app.domain.ResumoParticipacao;
import br.org.proximaetapa.app.ui.BaseActivity;
import br.org.proximaetapa.app.ui.adapter.IngressoAdapter;
import br.org.proximaetapa.app.ui.adapter.ParticipacaoAdapter;
import br.org.proximaetapa.app.ui.adapter.PresencaAdapter;
import br.org.proximaetapa.app.ui.convite.ConviteActivity;
import br.org.proximaetapa.app.ui.evento.EventoDetalheActivity;

/**
 * Meus ingressos (menu hambúrguer):
 *  - convites das palestras/cursos que ainda vão acontecer (entrada com QR Code);
 *  - participação em cada curso inscrito: presenças, faltas e % (RFM07);
 *  - histórico de cada presença registrada, mostrando se já foi sincronizada.
 */
public class MeusIngressosActivity extends BaseActivity {

    private AppRepository repo;
    private final PresencaAdapter presencaAdapter = new PresencaAdapter();
    private ParticipacaoAdapter participacaoAdapter;

    private List<InscricaoComEvento> inscricoes = new ArrayList<>();
    private List<Presenca> presencas = new ArrayList<>();
    private List<Evento> eventos = new ArrayList<>();
    private List<Certificado> certificados = new ArrayList<>();

    private TextView txtResumo, txtSemIngressos, txtSemParticipacao;
    private IngressoAdapter ingressoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_ingressos);
        configurarToolbar(R.string.menu_ingressos);
        repo = AppRepository.get(this);

        txtSemIngressos = findViewById(R.id.txt_sem_ingressos);
        txtSemParticipacao = findViewById(R.id.txt_sem_participacao);
        txtResumo = findViewById(R.id.txt_resumo_presenca);

        ingressoAdapter = new IngressoAdapter(new IngressoAdapter.Listener() {
            @Override
            public void aoAbrir(InscricaoComEvento item) {
                Intent i = new Intent(MeusIngressosActivity.this, ConviteActivity.class);
                i.putExtra(ConviteActivity.EXTRA_INSCRICAO_ID, item.inscricao.id);
                startActivity(i);
            }

            @Override
            public void aoCancelar(InscricaoComEvento item) {
                confirmarCancelamento(item);
            }
        });
        participacaoAdapter = new ParticipacaoAdapter(e -> {
            Intent i = new Intent(this, EventoDetalheActivity.class);
            i.putExtra(EventoDetalheActivity.EXTRA_EVENTO_ID, e.id);
            startActivity(i);
        });
        ((RecyclerView) findViewById(R.id.rv_ingressos)).setAdapter(ingressoAdapter);
        ((RecyclerView) findViewById(R.id.rv_participacao)).setAdapter(participacaoAdapter);
        ((RecyclerView) findViewById(R.id.rv_presencas)).setAdapter(presencaAdapter);

        // Tudo vem do banco local (Room): funciona sem internet
        repo.ingressos().observe(this, lista -> {
            inscricoes = lista == null ? new ArrayList<>() : lista;
            atualizar();
        });
        repo.presencas().observe(this, lista -> {
            presencas = lista == null ? new ArrayList<>() : lista;
            atualizar();
        });
        repo.eventos().observe(this, lista -> {
            eventos = lista == null ? new ArrayList<>() : lista;
            atualizar();
        });
        repo.certificados().observe(this, lista -> {
            certificados = lista == null ? new ArrayList<>() : lista;
            atualizar();
        });
    }

    private void atualizar() {
        long agora = System.currentTimeMillis();

        // 1) Ingressos: só eventos que ainda não terminaram
        List<InscricaoComEvento> futuros = new ArrayList<>();
        for (InscricaoComEvento i : inscricoes) {
            if (i.evento != null && !i.evento.jaTerminou(agora)) futuros.add(i);
        }
        ingressoAdapter.setItens(futuros);
        txtSemIngressos.setVisibility(futuros.isEmpty() ? View.VISIBLE : View.GONE);

        // 2) Participação em cada curso inscrito (inclusive os já concluídos)
        Map<Long, Integer> presPorEvento = new HashMap<>();
        for (Presenca p : presencas) {
            Integer n = presPorEvento.get(p.eventoId);
            presPorEvento.put(p.eventoId, n == null ? 1 : n + 1);
        }
        Set<Long> comCertificado = new HashSet<>();
        for (Certificado c : certificados) comCertificado.add(c.eventoId);

        List<ParticipacaoAdapter.Item> participacao = new ArrayList<>();
        for (InscricaoComEvento i : inscricoes) {
            Evento e = i.evento;
            if (e == null || e.isExterno()) continue;
            Integer p = presPorEvento.get(e.id);
            ResumoParticipacao r = ResumoParticipacao.calcular(e.dataInicio, e.dataFim,
                    e.totalEncontros, p == null ? 0 : p, agora, TimeZone.getDefault());
            participacao.add(new ParticipacaoAdapter.Item(e, r, comCertificado.contains(e.id)));
        }
        participacaoAdapter.setItens(participacao);
        txtSemParticipacao.setVisibility(participacao.isEmpty() ? View.VISIBLE : View.GONE);

        // 3) Histórico de presenças registradas
        presencaAdapter.setDados(presencas, eventos);
        int pendentes = 0;
        for (Presenca p : presencas) if (!p.sincronizado) pendentes++;
        String resumo = getResources().getQuantityString(R.plurals.presencas_registradas,
                presencas.size(), presencas.size());
        if (pendentes > 0) resumo += " · " + getString(R.string.aguardando_envio, pendentes);
        txtResumo.setText(resumo);
    }

    private void confirmarCancelamento(InscricaoComEvento item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancelar_inscricao_titulo)
                .setMessage(getString(R.string.cancelar_inscricao_msg, item.evento.titulo))
                .setNegativeButton(R.string.voltar, null)
                .setPositiveButton(R.string.cancelar_inscricao, (d, w) ->
                        repo.cancelarInscricao(item, new AppRepository.Callback<String>() {
                            @Override
                            public void sucesso(String msg) {
                                Snackbar.make(txtResumo, msg, Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void erro(String msg) {
                                Snackbar.make(txtResumo, msg, Snackbar.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }
}
