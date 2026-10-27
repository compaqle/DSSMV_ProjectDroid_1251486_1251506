package com.dssmv.plantkeep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.services.AvisoService;
import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.services.LocalizacaoService;
import com.dssmv.plantkeep.services.MeteoService;

import java.util.List;
import java.util.Map;

/**
 * Menu principal.
 */
public class MainActivity extends AppCompatActivity {

    private TextView txtEstadoAvisos;
    private ListView lvAvisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        txtEstadoAvisos = findViewById(R.id.txt_estado_avisos);
        lvAvisos = findViewById(R.id.lv_avisos);

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

    /** Atualiza os avisos sempre que o utilizador volta ao Menu */
    @Override
    protected void onResume() {
        super.onResume();
        if (!MeteorologiaContainer.getInstance().getPrevisaoHoraria().isEmpty()) {
            carregarAvisos();
        }
    }


    /** Pedido de permissões de GPS */
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
        txtEstadoAvisos.setText("A obter localização e meteorologia...");
        new LocalizacaoService(this).obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                new Thread(() -> {
                    try {
                        MeteoService.carregarMeteorologia(latitude, longitude);
                        mostraMensagem("Meteorologia atualizada!");
                        carregarAvisos();
                    } catch (Exception e) {
                        runOnUiThread(() -> txtEstadoAvisos.setText("Erro ao obter dados meteorológicos."));
                        mostraErro("Erro ao obter dados meteorológicos");
                    }
                }).start();
            }

            @Override
            public void onErro(String msg) {
                runOnUiThread(() -> txtEstadoAvisos.setText(msg));
                mostraErro(msg);
            }
        });
    }

    private void carregarAvisos() {
        runOnUiThread(() -> txtEstadoAvisos.setText("A verificar avisos para as tuas plantas..."));
        new Thread(() -> {
            try {
                final List<Aviso> avisos = AvisoService.obterAvisosAtuais();
                runOnUiThread(() -> mostrarAvisos(avisos));
            } catch (Exception e) {
                runOnUiThread(() -> txtEstadoAvisos.setText("Erro ao verificar avisos: " + e.getMessage()));
            }
        }).start();
    }

    private void mostrarAvisos(List<Aviso> avisos) {
        if (avisos == null || avisos.isEmpty()) {
            txtEstadoAvisos.setVisibility(View.VISIBLE);
            txtEstadoAvisos.setText("Sem avisos");
            lvAvisos.setAdapter(null);
        } else {
            txtEstadoAvisos.setVisibility(View.GONE);
            lvAvisos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, avisos));
        }
    }

    private void mostraMensagem(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}