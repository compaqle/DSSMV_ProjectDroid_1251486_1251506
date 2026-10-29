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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.AvisoService;
import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.services.LocalizacaoService;
import com.dssmv.plantkeep.services.MeteoService;
import com.dssmv.plantkeep.services.PlantaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menu principal.
 */
public class MainActivity extends AppCompatActivity {

    private TextView txtEstadoRegas;
    private ListView lvRegas;
    private final List<Aviso> listaRegas = new ArrayList<>();

    private TextView txtEstadoAvisos;
    private ListView lvAvisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        txtEstadoRegas = findViewById(R.id.txt_estado_regas);
        lvRegas = findViewById(R.id.lv_regas);

        txtEstadoAvisos = findViewById(R.id.txt_estado_avisos);
        lvAvisos = findViewById(R.id.lv_avisos);

        Button btnCriar = findViewById(R.id.btn_criar_planta);
        btnCriar.setOnClickListener(v -> startActivity(new Intent(this, CriarPlantaActivity.class)));

        Button btnVer = findViewById(R.id.btn_ver_plantas);
        btnVer.setOnClickListener(v -> startActivity(new Intent(this, PlantasActivity.class)));

        // Tocar numa planta da lista de "Próximas Regas" pergunta se já foi regada
        lvRegas.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaRegas.size()) {
                confirmarRega(listaRegas.get(position).getPlanta());
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!MeteorologiaContainer.getInstance().getPrevisaoHoraria().isEmpty()) {
            carregarAvisos();
        }
    }

    private void confirmarRega(Planta p) {
        if (p == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Regar planta")
                .setMessage("Marcar \"" + p.getNome() + "\" como regada hoje?")
                .setPositiveButton("Sim, reguei", (d, w) -> marcarComoRegada(p))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void marcarComoRegada(Planta p) {
        new Thread(() -> {
            try {
                PlantaService.regar(p);
                mostraMensagem(p.getNome() + " marcada como regada!");
                carregarAvisos();
            } catch (Exception e) {
                mostraErro("Não foi possível guardar a rega: " + e.getMessage());
            }
        }).start();
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
                    txtEstadoRegas.setText("Permissão de GPS negada.");
                    txtEstadoAvisos.setText("Permissão de GPS negada.");
                    mostraErro("Permissão de GPS negada.");
                }
            }
    );

    private void atualizarMeteorologia() {
        txtEstadoRegas.setText("A obter localização e meteorologia...");
        txtEstadoAvisos.setText("A obter localização e meteorologia...");

        new LocalizacaoService(this).obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                new Thread(() -> {
                    try {
                        MeteoService.carregarMeteorologia(latitude, longitude);
                        carregarAvisos();
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            txtEstadoRegas.setText("Erro ao obter dados meteorológicos.");
                            txtEstadoAvisos.setText("Erro ao obter dados meteorológicos.");
                        });
                        mostraErro("Erro ao obter dados meteorológicos");
                    }
                }).start();
            }

            @Override
            public void onErro(String msg) {
                runOnUiThread(() -> {
                    txtEstadoRegas.setText(msg);
                    txtEstadoAvisos.setText(msg);
                });
                mostraErro(msg);
            }
        });
    }

    private void carregarAvisos() {
        runOnUiThread(() -> {
            txtEstadoRegas.setText("A verificar próximas regas...");
            txtEstadoAvisos.setText("A verificar avisos meteorológicos...");
        });

        new Thread(() -> {
            try {
                final List<Aviso> todos = AvisoService.obterAvisosAtuais();
                runOnUiThread(() -> mostrarSecoes(todos));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtEstadoRegas.setText("Erro ao verificar regas: " + e.getMessage());
                    txtEstadoAvisos.setText("Erro ao verificar avisos: " + e.getMessage());
                });
            }
        }).start();
    }

    private void mostrarSecoes(List<Aviso> todos) {
        listaRegas.clear();
        List<Aviso> listaMeteorologia = new ArrayList<>();

        if (todos != null) {
            for (Aviso a : todos) {
                if (a.getTipo() == Aviso.TipoAviso.HUMIDADE_SOLO_BAIXA) {
                    listaRegas.add(a);
                } else {
                    listaMeteorologia.add(a);
                }
            }
        }

        listaRegas.sort((a1, a2) -> a1.getDataHora().compareTo(a2.getDataHora()));

        // Próximas Regas
        if (listaRegas.isEmpty()) {
            txtEstadoRegas.setVisibility(View.VISIBLE);
            txtEstadoRegas.setText("Nenhuma planta precisa de rega nos próximos dias.");
            lvRegas.setAdapter(null);
        } else {
            txtEstadoRegas.setVisibility(View.GONE);
            lvRegas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaRegas));
        }

        // Avisos Meteorológicos
        if (listaMeteorologia.isEmpty()) {
            txtEstadoAvisos.setVisibility(View.VISIBLE);
            txtEstadoAvisos.setText("Sem avisos");
            lvAvisos.setAdapter(null);
        } else {
            txtEstadoAvisos.setVisibility(View.GONE);
            lvAvisos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMeteorologia));
        }
    }

    private void mostraMensagem(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}