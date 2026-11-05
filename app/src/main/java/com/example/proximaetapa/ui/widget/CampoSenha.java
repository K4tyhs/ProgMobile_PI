package com.example.proximaetapa.ui.widget;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.proximaetapa.R;

/**
 * Botão "olho" para mostrar/ocultar a senha de um EditText comum.
 * O projeto usa apenas ConstraintLayout, por isso os campos são EditText
 * simples e este botão faz o papel do "olho" da senha.
 */
public final class CampoSenha {

    private CampoSenha() {
    }

    public static void configurar(EditText campo, ImageButton botao) {
        atualizar(campo, botao, false);
        botao.setOnClickListener(v -> {
            boolean estaOculta = campo.getTransformationMethod() instanceof PasswordTransformationMethod;
            atualizar(campo, botao, estaOculta);
        });
    }

    private static void atualizar(EditText campo, ImageButton botao, boolean mostrar) {
        int cursor = campo.getSelectionEnd();
        campo.setTransformationMethod(mostrar
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        if (cursor >= 0) campo.setSelection(Math.min(cursor, campo.length()));
        botao.setImageResource(mostrar ? R.drawable.ic_olho_off : R.drawable.ic_olho);
        // Leitor de tela anuncia a ação que o botão vai fazer
        botao.setContentDescription(campo.getContext().getString(
                mostrar ? R.string.ocultar_senha : R.string.mostrar_senha));
    }
}
