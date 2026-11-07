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

/**
 * Menu principal.
 */
public class MainActivity extends AppCompatActivity {

    private TextView txtEstadoAvisos;
    private ListView lvAvisos;
    private LocalizacaoProvider localizacaoProvider;
    private volatile boolean aCarregarMeteo = false;

    /** Pedido de permissões de GPS */
    private final ActivityResultLauncher<String[]> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.containsValue(false)) {
                    txtEstadoAvisos.setVisibility(View.VISIBLE);
                    txtEstadoAvisos.setText("Permissão de GPS negada.");
                    mostraErro("Permissão de GPS negada.");
                }
                // Se foi concedida, o onResume() é chamado automaticamente a seguir e inicia a meteorologia
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        txtEstadoAvisos = findViewById(R.id.txt_estado_avisos);
        lvAvisos = findViewById(R.id.lv_avisos);
        localizacaoProvider = new LocalizacaoService(this);

        Button btnCriar = findViewById(R.id.btn_criar_planta);
        btnCriar.setOnClickListener(v -> startActivity(new Intent(this, CriarPlantaActivity.class)));

        Button btnVer = findViewById(R.id.btn_ver_plantas);
        btnVer.setOnClickListener(v -> startActivity(new Intent(this, PlantasActivity.class)));

        // Pede permissão apenas se ainda não tiver (o onResume trata de carregar os dados)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean temPermissaoGPS = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!MeteorologiaContainer.getInstance().getPrevisaoHoraria().isEmpty()) {
            if (!aCarregarMeteo) {
                carregarAvisos();
            }
        } else if (temPermissaoGPS && !aCarregarMeteo) {
            atualizarMeteorologia();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        localizacaoProvider.pararObterLocalizacao();
        aCarregarMeteo = false;
    }

    private void atualizarMeteorologia() {
        if (aCarregarMeteo) {
            return;
        }
        aCarregarMeteo = true;
        txtEstadoAvisos.setVisibility(View.VISIBLE);
        txtEstadoAvisos.setText("A obter localização e meteorologia...");

        localizacaoProvider.obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                new Thread(() -> {
                    try {
                        MeteoService.carregarMeteorologia(latitude, longitude);
                        carregarAvisos();
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            txtEstadoAvisos.setVisibility(View.VISIBLE);
                            txtEstadoAvisos.setText("Erro ao obter dados meteorológicos.");
                        });
                        mostraErro("Erro ao obter dados meteorológicos");
                    } finally {
                        aCarregarMeteo = false;
                    }
                }).start();
            }

            @Override
            public void onErro(String msg) {
                aCarregarMeteo = false;
                runOnUiThread(() -> {
                    txtEstadoAvisos.setVisibility(View.VISIBLE);
                    txtEstadoAvisos.setText(msg);
                });
                mostraErro(msg);
            }
        });
    }

    private void carregarAvisos() {
        runOnUiThread(() -> {
            txtEstadoAvisos.setVisibility(View.VISIBLE);
            txtEstadoAvisos.setText("A verificar avisos meteorológicos...");
        });
        new Thread(() -> {
            try {
                final List<Aviso> avisos = AvisoService.obterAvisosAtuais();
                runOnUiThread(() -> mostrarAvisos(avisos));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtEstadoAvisos.setVisibility(View.VISIBLE);
                    txtEstadoAvisos.setText("Erro ao verificar avisos: " + e.getMessage());
                });
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

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}