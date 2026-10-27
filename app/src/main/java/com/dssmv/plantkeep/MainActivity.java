package com.dssmv.plantkeep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.services.LocalizacaoService;
import com.dssmv.plantkeep.services.MeteoService;

import java.util.Map;

/**
 * Menu principal.
 */
public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnCriar = findViewById(R.id.btn_criar_planta);
        btnCriar.setOnClickListener(v -> startActivity(new Intent(this, CriarPlantaActivity.class)));

        Button btnVer = findViewById(R.id.btn_ver_plantas);
        btnVer.setOnClickListener(v -> startActivity(new Intent(this, PlantasActivity.class)));

        // Verifica permissões
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            atualizarMeteorologia();
        }
    }

    // Pedido de permissões
    private final ActivityResultLauncher<String[]> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                    }
                }
                if (allGranted) {
                    atualizarMeteorologia();
                } else {
                    mostraErro("Permissão de GPS negada.");
                }
            }
    );

    private void atualizarMeteorologia() {
        new LocalizacaoService(this).obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                new Thread(() -> {
                    try {
                        MeteoService.carregarMeteorologia(latitude, longitude);
                        mostraMensagem("Meteorologia atualizada!");
                    } catch (Exception e) {
                        mostraErro("Erro ao obter dados meteorológicos");
                    }
                }).start();
            }

            @Override
            public void onErro(String msg) {
                mostraErro(msg);
            }
        });
    }

    private void mostraMensagem(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}