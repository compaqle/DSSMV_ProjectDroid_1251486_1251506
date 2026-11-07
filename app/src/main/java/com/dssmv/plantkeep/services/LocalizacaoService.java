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
    private final LocationManager locationManager;
    private BroadcastReceiver gpsReceiver;
    private LocationListener locationListener;
    private Callback callbackAtivo;

    public LocalizacaoService(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    /** Usa GPS e Network para obter a localização */
    @SuppressLint("MissingPermission")
    @Override
    public void obterLocalizacao(Callback listener) {
        pararObterLocalizacao(); // Cancela qualquer pedido anterior
        this.callbackAtivo = listener;

        if (locationManager == null) {
            notificarErro("Serviço de localização indisponível.");
            return;
        }

        boolean gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean redeAtiva = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsAtivo && !redeAtiva) {
            notificarErro("Por favor, ative a localização do telemóvel.");
            esperarGpsEObterLocalizacao();
            return;
        }

        tentarObterLocalizacao(gpsAtivo, redeAtiva);
    }

    /** Interrompe qualquer pedido */
    @Override
    public void pararObterLocalizacao() {
        this.callbackAtivo = null;
        removerReceiver();
        removerLocationUpdates();
    }

    /** Regista um BroadcastReceiver que fica à espera que o utilizador ligue a localização */
    private void esperarGpsEObterLocalizacao() {
        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean rede = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (gps || rede) {
                    removerReceiver();
                    tentarObterLocalizacao(gps, rede);
                }
            }
        };
        context.registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    /** Tenta obter a localização usando cache, se não houver pede uma localização nova */
    @SuppressLint("MissingPermission")
    private void tentarObterLocalizacao(boolean gpsAtivo, boolean redeAtiva) {
        Location ultimaLocalizacao = null;
        if (gpsAtivo) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (ultimaLocalizacao == null && redeAtiva) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (ultimaLocalizacao != null) {
            notificarLocalizacaoELimpar(ultimaLocalizacao.getLatitude(), ultimaLocalizacao.getLongitude());
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                notificarLocalizacaoELimpar(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        String provider;
        if (gpsAtivo) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());
    }

    private void removerReceiver() {
        if (gpsReceiver != null) {
            try {
                context.unregisterReceiver(gpsReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            gpsReceiver = null;
        }
    }

    private void removerLocationUpdates() {
        if (locationListener != null && locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ignored) {
            }
            locationListener = null;
        }
    }

    private void notificarLocalizacaoELimpar(double lat, double lon) {
        Callback cb = callbackAtivo;
        pararObterLocalizacao();
        if (cb != null) {
            cb.onLocalizacao(lat, lon);
        }
    }

    private void notificarErro(String msg) {
        if (callbackAtivo != null) {
            callbackAtivo.onErro(msg);
        }
    }
}