package br.org.proximaetapa.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.Certificado;
import br.org.proximaetapa.app.data.local.entity.Evento;

/** Grade de cursos: os concluídos ficam destacados e abrem o certificado. */
public class CertificadoAdapter extends RecyclerView.Adapter<CertificadoAdapter.VH> {

    /** Um curso e, se houver, o certificado dele. */
    public static class Item {
        public final Evento evento;
        public final Certificado certificado;
        public final int presencas;

        public Item(Evento evento, Certificado certificado, int presencas) {
            this.evento = evento;
            this.certificado = certificado;
            this.presencas = presencas;
        }
    }

    public interface Listener {
        void aoTocar(Item item);
    }

    private final List<Item> itens = new ArrayList<>();
    private final Listener listener;

    public CertificadoAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<Item> novos) {
        Diff.aplicar(this, itens, novos, i -> i.evento.id,
                i -> Diff.assinatura(i.evento.titulo, i.certificado == null ? 0 : i.certificado.id, i.presencas));
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificado, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = itens.get(position);
        boolean concluido = it.certificado != null;
        h.instituicao.setText(it.evento.instituicao);
        h.instituicao.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.pe_texto_sec));
        h.curso.setText(it.evento.titulo);

        int corFundo = ContextCompat.getColor(h.itemView.getContext(),
                concluido ? R.color.pe_verde_claro : R.color.pe_superficie_alt);
        h.card.setCardBackgroundColor(corFundo);
        h.curso.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                concluido ? R.color.pe_ink : R.color.pe_texto));
        h.icone.setImageResource(concluido ? R.drawable.ic_download : R.drawable.ic_cadeado);
        h.icone.setColorFilter(ContextCompat.getColor(h.itemView.getContext(),
                concluido ? R.color.pe_verde_texto : R.color.pe_texto_sec));
        if (concluido) {
            // Fundo verde claro fixo (também no tema escuro): texto escuro para contraste
            h.instituicao.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.pe_ink));
        }

        h.itemView.setContentDescription(it.evento.titulo + ", " + it.evento.instituicao + ". "
                + (concluido ? "Certificado disponível, toque para baixar." : "Certificado ainda não liberado."));
        h.itemView.setOnClickListener(v -> listener.aoTocar(it));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final ImageView icone;
        final TextView instituicao, curso;

        VH(View v) {
            super(v);
            card = v.findViewById(R.id.card);
            icone = v.findViewById(R.id.img_status);
            instituicao = v.findViewById(R.id.txt_instituicao);
            curso = v.findViewById(R.id.txt_curso);
        }
    }
}
