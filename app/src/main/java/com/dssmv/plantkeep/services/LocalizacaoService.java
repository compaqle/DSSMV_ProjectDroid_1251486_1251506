package com.dssmv.plantkeep.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

public class LocalizacaoService implements LocalizacaoProvider {
    private final Context context;

    public LocalizacaoService(Context context) {
        this.context = context.getApplicationContext();
    }

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
            listener.onErro("Por favor, ative o GPS do telemóvel.");
            return;
        }

        // 1. Tenta obter a última localização em cache (resposta imediata)
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

        String provider = gpsAtivo ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;

        LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(context),
                location -> {
                    if (location != null) {
                        listener.onLocalizacao(location.getLatitude(), location.getLongitude());
                    } else {
                        listener.onErro("Não foi possível obter a localização atual.");
                    }
                }
        );
    }
}