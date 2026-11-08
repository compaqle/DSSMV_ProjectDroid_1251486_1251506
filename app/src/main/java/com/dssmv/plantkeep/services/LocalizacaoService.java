package com.dssmv.plantkeep.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * Serviço da localização.
 * Obtem a localização via GPS ou via Rede/Wifi
 */

public class LocalizacaoService implements LocalizacaoProvider {

    private static final long TIMEOUT_GPS_MS = 10000L; // 10 segundos

    private final Context context;
    private final LocationManager locationManager;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private BroadcastReceiver gpsReceiver;
    private LocationListener locationListener;
    private Callback callbackAtivo;

    public LocalizacaoService(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    // Usa GPS ou Rede para obter a localização conforme as permissões
    @SuppressLint("MissingPermission")
    @Override
    public void obterLocalizacao(Callback listener) {
        pararObterLocalizacao(); // Cancela qualquer pedido anterior
        this.callbackAtivo = listener;

        if (locationManager == null) {
            notificarErro("Serviço de localização indisponível.");
            return;
        }

        if (!temPermissaoFine() && !temPermissaoCoarse()) {
            notificarErro("Permissão de GPS negada.");
            return;
        }

        boolean gpsAtivo = podeUsarGps();
        boolean redeAtiva = podeUsarRede();

        if (!gpsAtivo && !redeAtiva) {
            notificarErro("Por favor, ative a localização do telemóvel.");
            esperarGpsEObterLocalizacao();
            return;
        }

        tentarObterLocalizacao(gpsAtivo, redeAtiva);
    }

    // Interrompe qualquer pedido e limpa recursos
    @Override
    public void pararObterLocalizacao() {
        this.callbackAtivo = null;
        cancelarTimeout();
        removerReceiver();
        removerLocationUpdates();
    }

    // Fica à espera que o utilizador ligue a localização no telemóvel
    private void esperarGpsEObterLocalizacao() {
        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                boolean gps = podeUsarGps();
                boolean rede = podeUsarRede();
                if (gps || rede) {
                    removerReceiver();
                    tentarObterLocalizacao(gps, rede);
                }
            }
        };
        context.registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    // Tenta obter localização em cache; se não houver, pede uma nova com timeout
    @SuppressLint("MissingPermission")
    private void tentarObterLocalizacao(boolean gpsAtivo, boolean redeAtiva) {
        Location ultimaLocalizacao = obterUltimaLocalizacaoConhecida(gpsAtivo, redeAtiva);

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

        String provider = gpsAtivo ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());

        timeoutRunnable = () -> {
            removerLocationUpdates();
            Location fallback = obterUltimaLocalizacaoConhecida(podeUsarGps(), podeUsarRede());
            if (fallback != null) {
                notificarLocalizacaoELimpar(fallback.getLatitude(), fallback.getLongitude());
            } else {
                Callback cb = callbackAtivo;
                pararObterLocalizacao();
                if (cb != null) {
                    cb.onErro("Não foi possível obter a localização (sinal GPS fraco).");
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_GPS_MS);
    }

    @SuppressLint("MissingPermission")
    private Location obterUltimaLocalizacaoConhecida(boolean gpsAtivo, boolean redeAtiva) {
        Location ultimaLocalizacao = null;
        if (gpsAtivo) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (ultimaLocalizacao == null && redeAtiva) {
            ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return ultimaLocalizacao;
    }

    // Só pode usar GPS se o hardware estiver ativo e tiver permissão FINE
    private boolean podeUsarGps() {
        return locationManager != null
                && temPermissaoFine()
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Pode usar Rede se estiver ativa e tiver permissão FINE ou COARSE
    private boolean podeUsarRede() {
        return locationManager != null
                && (temPermissaoFine() || temPermissaoCoarse())
                && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean temPermissaoFine() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean temPermissaoCoarse() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void cancelarTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
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