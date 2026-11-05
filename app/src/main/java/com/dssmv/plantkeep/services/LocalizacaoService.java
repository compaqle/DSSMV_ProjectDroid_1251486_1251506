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

    public LocalizacaoService(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Usa GPS e Network para obter a localizacao */
    @SuppressLint("MissingPermission")
    @Override
    public void obterLocalizacao(Callback listener) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            listener.onErro("Serviço de localização indisponível.");
            return;
        }

        boolean gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean redeAtiva = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsAtivo && !redeAtiva) {
            listener.onErro("Por favor, ative a localização do telemóvel.");
            // BroadcastReceiver que fica à espera até a localizaçao ser ligada
            esperarGpsEObterLocalizacao(locationManager, listener);
            return;
        }

        tentarObterLocalizacao(locationManager, gpsAtivo, redeAtiva, listener);
    }

    /** Regista um BroadcastReceiver que fica à espera que o utilizador ligue a localizaçao */
    private void esperarGpsEObterLocalizacao(LocationManager locationManager, Callback listener) {
        BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                // O Android envia este broadcast sempre que o estado dos providers muda

                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean rede = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (gps || rede) {
                    context.unregisterReceiver(this);
                    tentarObterLocalizacao(locationManager, gps, rede, listener);
                }
            }
        };

        IntentFilter filtro = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(gpsReceiver, filtro);
    }

    /** Tenta obter a localização usando cache, se não houver pede uma localizacao nova */
    @SuppressLint("MissingPermission")
    private void tentarObterLocalizacao(LocationManager locationManager,
                                        boolean gpsAtivo, boolean redeAtiva,
                                        Callback listener) {
        // Verifica se há localização em cache
        Location ultimaLocalizacao = null;
        if (gpsAtivo) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (ultimaLocalizacao == null && redeAtiva) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (ultimaLocalizacao != null) {
            listener.onLocalizacao(ultimaLocalizacao.getLatitude(), ultimaLocalizacao.getLongitude());
            return;
        }
        // Se não houver em cache, pede atualização ao LocationManager

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this);
                new Thread(() -> {
                    if (location != null) {
                        listener.onLocalizacao(location.getLatitude(), location.getLongitude());
                    } else {
                        listener.onErro("Não foi possível obter a localização atual.");
                    }
                }).start();
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

        // Devolve a localizacao à thread principal
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());
    }
}
