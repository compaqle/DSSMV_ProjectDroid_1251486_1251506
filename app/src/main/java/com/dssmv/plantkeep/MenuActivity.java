package com.dssmv.plantkeep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.services.LocalizacaoService;

/**
 * Menu principal.
 */
public class MenuActivity extends AppCompatActivity {

    private static final int CODIGO_PERMISSAO_GPS = 100;
    private MeteorologiaContainer meteoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        meteoService = MeteorologiaContainer.getInstance();

        Button btnCriar = findViewById(R.id.btn_criar_planta);
        btnCriar.setOnClickListener(v -> startActivity(new Intent(this, CriarPlantaActivity.class)));

        Button btnVer = findViewById(R.id.btn_ver_plantas);
        btnVer.setOnClickListener(v -> startActivity(new Intent(this, PlantasActivity.class)));

        // Pedido de permissao para usar o GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISSAO_GPS);
        } else {
            atualizarMeteorologia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISSAO_GPS && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            atualizarMeteorologia();
        } else {
            mostraErro("Permissão de GPS negada.");
        }
    }

    private void atualizarMeteorologia() {
        new LocalizacaoService(this).obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                meteoService.carregarMeteorologia(latitude, longitude, new MeteorologiaContainer.Callback() {
                    @Override
                    public void onSucesso(MeteorologiaContainer container) {
                        mostraMensagem("Meteorologia atualizada!");
                    }

                    @Override
                    public void onErro(String msg) {
                        mostraErro(msg);
                    }
                });
            }

            @Override
            public void onErro(String msg) {
                mostraErro(msg);
            }
        });
    }

    private void mostraMensagem(String msg) {
        runOnUiThread(() -> Toast.makeText(MenuActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}
