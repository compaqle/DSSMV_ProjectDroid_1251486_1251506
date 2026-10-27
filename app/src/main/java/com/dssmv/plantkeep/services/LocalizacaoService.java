package com.dssmv.plantkeep.services;

import android.annotation.SuppressLint;
import android.content.Context;
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

    //Usa GPS e Network para obter a localizacao
    @SuppressLint("MissingPermission")
    @Override
    public void obterLocalizacao(Callback listener) {
        new Thread(() -> {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager == null) {
                listener.onErro("Serviço de localização indisponível.");
                return;
            }

            boolean gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean redeAtiva = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!gpsAtivo && !redeAtiva) {
                listener.onErro("Por favor, ative o GPS do telemóvel.");
                return;
            }

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

            // Se não houver cache, pede atualização ao LocationManager
            String provider = gpsAtivo ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;

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
            // Devolve a localizacao à thread principal
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());
        }).start();
    }
}