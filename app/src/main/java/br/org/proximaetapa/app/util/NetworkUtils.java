package br.org.proximaetapa.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/** Verifica se há internet (tratamento de perda de conexão - RFM15). */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean estaOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network rede = cm.getActiveNetwork();
        if (rede == null) return false;
        NetworkCapabilities cap = cm.getNetworkCapabilities(rede);
        return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
