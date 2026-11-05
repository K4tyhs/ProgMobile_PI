package br.org.proximaetapa.app.ui.widget;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Barra de progresso feita só com ConstraintLayout: um View de "trilho" e um
 * View de "preenchimento" cuja largura é uma porcentagem da tela
 * (layout_constraintWidth_percent).
 */
public final class BarraPercentual {

    private BarraPercentual() {
    }

    /**
     * @param preenchimento View com layout_width="0dp" e
     *                      layout_constraintWidth_default="percent"
     * @param percentual    0 a 100
     */
    public static void definir(View preenchimento, int percentual) {
        int p = Math.max(0, Math.min(100, percentual));
        if (p == 0) {
            // Percentual 0 faria a view ocupar a largura toda (0 = "sem limite")
            preenchimento.setVisibility(View.INVISIBLE);
            return;
        }
        preenchimento.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) preenchimento.getLayoutParams();
        lp.matchConstraintPercentWidth = p / 100f;
        preenchimento.setLayoutParams(lp);
    }
}
