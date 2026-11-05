package com.example.proximaetapa.util;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.example.proximaetapa.R;

/**
 * Capa dos cards por instituição: usa a foto da fachada do Figma quando
 * existe (ESPM, FECAP, Inteli) e, para as demais, uma capa colorida com o
 * nome da instituição. Todas as cores têm contraste suficiente com texto branco.
 */
public final class CapaUtils {

    private CapaUtils() {
    }

    public static int corDaInstituicao(String instituicao) {
        if (instituicao == null) return Color.parseColor("#6F2FB2");
        switch (instituicao) {
            case "FECAP":
                return Color.parseColor("#0B6B3A");
            case "ESPM":
                return Color.parseColor("#B3122E");
            case "PUC":
            case "PUC-SP":
                return Color.parseColor("#123E7C");
            case "São Camilo":
                return Color.parseColor("#0E6E8C");
            case "Inteli":
                return Color.parseColor("#3D2C8D");
            case "FGV":
                return Color.parseColor("#1D3F6E");
            case "FAAP":
                return Color.parseColor("#7A1F3D");
            case "Insper":
                return Color.parseColor("#A01C2C");
            case "INEP":
            case "MEC":
                return Color.parseColor("#205E2E");
            case "Próxima Etapa":
                return Color.parseColor("#4A1080");
            default:
                // Cor estável derivada do nome, sempre escura o bastante
                float[] hsv = {Math.floorMod(instituicao.hashCode(), 360), 0.65f, 0.45f};
                return Color.HSVToColor(hsv);
        }
    }

    /** Foto da fachada da instituição (Figma), ou 0 se não houver. */
    @DrawableRes
    public static int fotoDaInstituicao(String instituicao) {
        if (instituicao == null) return 0;
        switch (instituicao) {
            case "ESPM":
                return R.drawable.foto_espm;
            case "FECAP":
                return R.drawable.foto_fecap;
            case "Inteli":
                return R.drawable.foto_inteli;
            default:
                return 0;
        }
    }

    /**
     * Capa de card com foto: mostra a fachada da instituição ou, sem foto,
     * a cor da instituição com o nome escrito por cima.
     */
    public static void aplicarFoto(ImageView foto, TextView texto, String instituicao) {
        int res = fotoDaInstituicao(instituicao);
        if (res != 0) {
            foto.setImageResource(res);
            if (texto != null) texto.setVisibility(View.GONE);
        } else {
            foto.setImageDrawable(new ColorDrawable(corDaInstituicao(instituicao)));
            if (texto != null) {
                texto.setVisibility(View.VISIBLE);
                texto.setText(instituicao == null ? "" : instituicao);
                texto.setTextColor(Color.WHITE);
            }
        }
    }

    /** Pinta a capa e escreve o nome da instituição. */
    public static void aplicar(View capa, TextView texto, String instituicao, float raioPx) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(corDaInstituicao(instituicao));
        fundo.setCornerRadius(raioPx);
        capa.setBackground(fundo);
        if (texto != null) {
            texto.setText(instituicao == null ? "" : instituicao);
            texto.setTextColor(Color.WHITE);
        }
    }

    /** Cor da barra lateral por categoria (cards de cursos da Home). */
    public static int corDaCategoria(String categoria) {
        if (categoria == null) return Color.parseColor("#F2C744");
        switch (categoria) {
            case "Carreira":
                return Color.parseColor("#F2C744");
            case "Comunicação":
                return Color.parseColor("#51C600");
            case "Tecnologia":
                return Color.parseColor("#7730E2");
            case "Saúde e bem-estar":
                return Color.parseColor("#F16161");
            case "Cidadania":
                return Color.parseColor("#0E6E8C");
            default:
                return Color.parseColor("#FBDE89");
        }
    }
}
