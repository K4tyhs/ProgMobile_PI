package br.org.proximaetapa.app.ui.convite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.InscricaoComEvento;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.data.repository.AppRepository;
import br.org.proximaetapa.app.domain.ParticipacaoCalculator;
import br.org.proximaetapa.app.ui.BaseActivity;
import br.org.proximaetapa.app.util.DateUtils;
import br.org.proximaetapa.app.util.QrUtils;
import br.org.proximaetapa.app.util.SessionManager;

/**
 * Convite do aluno + registro de presença por QR Code (RFM06 - função
 * principal da Entrega 2). Fluxo:
 *   QR Code/Check-in -> validação -> persistência local (Room)
 *   -> envio à API (ou fila offline) -> sincronização -> histórico.
 */
public class ConviteActivity extends BaseActivity {

    public static final String EXTRA_INSCRICAO_ID = "inscricao_id";

    private AppRepository repo;
    private InscricaoComEvento item;
    private boolean mostrandoVerso = false;

    private View cardFrente, cardVerso, progresso;
    private TextView txtStatus, txtParticipacao;
    private MaterialButton btnCheckin, btnCodigo;

    /** Leitor de QR Code (ZXing) aberto com a Activity Result API. */
    private final ActivityResultLauncher<ScanOptions> leitorQr =
            registerForActivityResult(new ScanContract(), resultado -> {
                if (resultado.getContents() == null) return; // usuário cancelou
                registrar(resultado.getContents(), false);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convite);
        configurarToolbar(R.string.convite_titulo);
        repo = AppRepository.get(this);

        cardFrente = findViewById(R.id.card_frente);
        cardVerso = findViewById(R.id.card_verso);
        progresso = findViewById(R.id.progresso);
        txtStatus = findViewById(R.id.txt_status_presenca);
        txtParticipacao = findViewById(R.id.txt_participacao);
        btnCheckin = findViewById(R.id.btn_checkin);
        btnCodigo = findViewById(R.id.btn_codigo);

        float distancia = 8000 * getResources().getDisplayMetrics().density;
        cardFrente.setCameraDistance(distancia);
        cardVerso.setCameraDistance(distancia);
        cardFrente.setOnClickListener(v -> virarCartao());
        cardVerso.setOnClickListener(v -> virarCartao());

        btnCheckin.setOnClickListener(v -> abrirLeitor());
        btnCodigo.setOnClickListener(v -> pedirCodigo());

        String inscricaoId = getIntent().getStringExtra(EXTRA_INSCRICAO_ID);
        repo.ingresso(inscricaoId).observe(this, i -> {
            item = i;
            mostrarConvite();
            observarPresencas();
        });
    }

    private boolean presencasObservadas = false;

    private void observarPresencas() {
        if (presencasObservadas || item == null) return;
        presencasObservadas = true;
        repo.presencasDoEvento(item.inscricao.eventoId).observe(this, this::mostrarPresencas);
    }

    private void mostrarConvite() {
        if (item == null || item.evento == null) return;
        Evento e = item.evento;
        String nome = SessionManager.get(this).getNome();
        String quando = DateUtils.periodo(e.dataInicio, e.dataFim);
        ((TextView) findViewById(R.id.txt_convite)).setText(
                getString(R.string.convite_texto, nome, e.titulo, e.local, quando));
        ((TextView) findViewById(R.id.txt_verso_titulo)).setText(e.titulo);
        ((TextView) findViewById(R.id.txt_codigo_ingresso)).setText(item.inscricao.codigoIngresso);

        // QR do ingresso: identifica aluno + inscrição na entrada do evento
        Bitmap qr = QrUtils.gerar("PE:INGRESSO:" + item.inscricao.codigoIngresso + ":" + item.inscricao.id, 600);
        if (qr != null) ((ImageView) findViewById(R.id.img_qr)).setImageBitmap(qr);

        boolean encerrado = e.jaTerminou(System.currentTimeMillis());
        btnCheckin.setEnabled(!encerrado);
        btnCodigo.setEnabled(!encerrado);
    }

    private void mostrarPresencas(List<Presenca> lista) {
        if (item == null || item.evento == null) return;
        int total = item.evento.totalEncontros;
        int qtd = lista == null ? 0 : lista.size();
        txtParticipacao.setText(getString(R.string.participacao_evento,
                ParticipacaoCalculator.percentual(qtd, total), qtd, total));

        Presenca hoje = null;
        if (lista != null) {
            for (Presenca p : lista) {
                if (DateUtils.hojeIso().equals(p.dataEncontro)) hoje = p;
            }
        }
        if (hoje == null) {
            txtStatus.setVisibility(View.GONE);
        } else {
            txtStatus.setVisibility(View.VISIBLE);
            txtStatus.setText(getString(hoje.sincronizado
                            ? R.string.presenca_hoje_enviada
                            : R.string.presenca_hoje_pendente,
                    DateUtils.hora(hoje.registradoEm)));
        }
    }

    /** Animação de "virar" o convite (frente <-> QR Code). */
    private void virarCartao() {
        View sai = mostrandoVerso ? cardVerso : cardFrente;
        View entra = mostrandoVerso ? cardFrente : cardVerso;
        ObjectAnimator meia = ObjectAnimator.ofFloat(sai, "rotationY", 0f, 90f);
        meia.setDuration(180);
        meia.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sai.setVisibility(View.INVISIBLE); // INVISIBLE mantém o tamanho (o verso usa a frente como referência)
                sai.setRotationY(0f);
                entra.setRotationY(-90f);
                entra.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(entra, "rotationY", -90f, 0f).setDuration(180).start();
            }
        });
        meia.start();
        mostrandoVerso = !mostrandoVerso;
    }

    private void abrirLeitor() {
        ScanOptions opcoes = new ScanOptions();
        opcoes.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        opcoes.setPrompt(getString(R.string.leitor_prompt));
        opcoes.setBeepEnabled(false);
        opcoes.setOrientationLocked(false);
        leitorQr.launch(opcoes);
    }

    /** Alternativa acessível ao QR Code (câmera ruim, baixa visão...). */
    private void pedirCodigo() {
        View campo = LayoutInflater.from(this).inflate(R.layout.dialog_campo_texto, null);
        TextView rotulo = campo.findViewById(R.id.rotulo_campo);
        EditText edt = campo.findViewById(R.id.edt_campo);
        rotulo.setText(R.string.codigo_evento);
        edt.setHint(R.string.hint_codigo);
        edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.digitar_codigo)
                .setMessage(R.string.digitar_codigo_msg)
                .setView(campo)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.confirmar, (d, w) -> {
                    String codigo = edt.getText() == null ? "" : edt.getText().toString();
                    registrar(codigo, true);
                })
                .show();
    }

    private void registrar(String leitura, boolean digitado) {
        if (item == null) return;
        progresso.setVisibility(View.VISIBLE);
        btnCheckin.setEnabled(false);
        repo.registrarPresenca(item.inscricao.eventoId, leitura, digitado, new AppRepository.Callback<String>() {
            @Override
            public void sucesso(String msg) {
                progresso.setVisibility(View.GONE);
                btnCheckin.setEnabled(true);
                new MaterialAlertDialogBuilder(ConviteActivity.this)
                        .setIcon(R.drawable.ic_check)
                        .setTitle(R.string.presenca_ok_titulo)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }

            @Override
            public void erro(String msg) {
                progresso.setVisibility(View.GONE);
                btnCheckin.setEnabled(true);
                new MaterialAlertDialogBuilder(ConviteActivity.this)
                        .setTitle(R.string.presenca_erro_titulo)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        });
    }
}
