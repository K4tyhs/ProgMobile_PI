package br.org.proximaetapa.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.proximaetapa.app.R;
import br.org.proximaetapa.app.data.local.entity.Evento;
import br.org.proximaetapa.app.data.local.entity.Presenca;
import br.org.proximaetapa.app.util.DateUtils;

/** Histórico de presença (RFM07), mostrando se já foi sincronizada com a API. */
public class PresencaAdapter extends RecyclerView.Adapter<PresencaAdapter.VH> {

    private final List<Presenca> itens = new ArrayList<>();
    private final Map<Long, Evento> eventos = new HashMap<>();

    public void setDados(List<Presenca> presencas, List<Evento> listaEventos) {
        boolean eventosMudaram = listaEventos != null && listaEventos.size() != eventos.size();
        eventos.clear();
        if (listaEventos != null) for (Evento e : listaEventos) eventos.put(e.id, e);
        Diff.aplicar(this, itens, presencas, p -> p.id,
                p -> Diff.assinatura(p.sincronizado, p.registradoEm, p.origem));
        // Os títulos vêm dos eventos: se eles chegaram depois, redesenha os nomes
        if (eventosMudaram) notifyItemRangeChanged(0, itens.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_presenca, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Presenca p = itens.get(position);
        Evento e = eventos.get(p.eventoId);
        h.evento.setText(e == null ? "Evento #" + p.eventoId : e.titulo);
        String origem = Presenca.ORIGEM_CODIGO.equals(p.origem) ? "código digitado" : "QR Code";
        String sync = p.sincronizado ? "sincronizada" : "aguardando internet";
        h.detalhe.setText(DateUtils.dataHora(p.registradoEm) + " · " + origem + " · " + sync);
        int cor = ContextCompat.getColor(h.itemView.getContext(),
                p.sincronizado ? R.color.pe_verde_texto : R.color.pe_coral_texto);
        h.icone.setImageResource(p.sincronizado ? R.drawable.ic_check : R.drawable.ic_sync);
        h.icone.setColorFilter(cor);
        h.itemView.setContentDescription("Presença em " + h.evento.getText() + ", " + h.detalhe.getText());
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView icone;
        final TextView evento, detalhe;

        VH(View v) {
            super(v);
            icone = v.findViewById(R.id.img_status);
            evento = v.findViewById(R.id.txt_evento);
            detalhe = v.findViewById(R.id.txt_detalhe);
        }
    }
}
