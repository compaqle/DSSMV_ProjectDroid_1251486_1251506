package com.dssmv.plantkeep.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;

public class LocalizacaoService implements LocalizacaoProvider {

    private final Context context;
    private LocationManager locationManager;
    private BroadcastReceiver gpsReceiver;
    private LocationListener locationListener;
    private Callback callbackAtivo;

    public LocalizacaoService(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Usa GPS e Network para obter a localização */
    @SuppressLint("MissingPermission")
    @Override
    public void obterLocalizacao(Callback listener) {
        pararObterLocalizacao(); // Antes de arrancar um novo pedido, cancela qualquer pedido anterior
        this.callbackAtivo = listener;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            notificarErro("Serviço de localização indisponível.");
            return;
        }

        boolean gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean redeAtiva = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsAtivo && !redeAtiva) {
            notificarErro("Por favor, ative a localização do telemóvel.");
            // Fica à espera com BroadcastReceiver até que o utilizador ligue o GPS
            esperarGpsEObterLocalizacao();
            return;
        }

        tentarObterLocalizacao(gpsAtivo, redeAtiva);
    }

    /** Interrompe qualquer pedido  */
    @Override
    public void pararObterLocalizacao() {
        this.callbackAtivo = null;
        removerReceiver();
        removerLocationUpdates();
    }

    /** Regista um BroadcastReceiver que fica à espera que o utilizador ligue a localização */
    private void esperarGpsEObterLocalizacao() {
        removerReceiver();

        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (locationManager == null) return;

                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean rede = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (gps || rede) {
                    // Quando o utilizador liga a localização, remove este BroadcastReceiver
                    removerReceiver();
                    tentarObterLocalizacao(gps, rede);
                }
            }
        };

        IntentFilter filtro = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(gpsReceiver, filtro);
    }

    /** Tenta obter a localização usando cache, se não houver pede uma localização nova */
    @SuppressLint("MissingPermission")
    private void tentarObterLocalizacao(boolean gpsAtivo, boolean redeAtiva) {
        if (locationManager == null) return;

        // Verifica se existe alguma localização em cache
        Location ultimaLocalizacao = null;
        if (gpsAtivo) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (ultimaLocalizacao == null && redeAtiva) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (ultimaLocalizacao != null) {
            notificarLocalizacao(ultimaLocalizacao.getLatitude(), ultimaLocalizacao.getLongitude());
            return;
        }

        // Se não houver em cache, pede a localização ao LocationManager
        removerLocationUpdates();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                removerLocationUpdates(); // Ao receber uma localizaçao válida, para de atualizar a localização

                new Thread(() -> {
                    notificarLocalizacao(location.getLatitude(), location.getLongitude());
                }).start();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        String provider = gpsAtivo ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());
    }

    /** Remove o BroadcastReceiver  */
    private void removerReceiver() {
        if (gpsReceiver != null) {
            try {
                context.unregisterReceiver(gpsReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            gpsReceiver = null;
        }
    }

    /** Pede ao LocationManager para parar de atualizar a localização */
    private void removerLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ignored) {
            }
            locationListener = null;
        }
    }

    private void notificarLocalizacao(double lat, double lon) {
        Callback cb = callbackAtivo;
        if (cb != null) {
            cb.onLocalizacao(lat, lon);
        }
    }

    private void notificarErro(String msg) {
        Callback cb = callbackAtivo;
        if (cb != null) {
            cb.onErro(msg);
        }
    }
}
