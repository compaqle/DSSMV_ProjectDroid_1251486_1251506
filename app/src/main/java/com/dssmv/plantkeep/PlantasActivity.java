package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantaService;

import java.util.ArrayList;
import java.util.List;

/**
 * Ecrã que mostra a lista de plantas.
 */
public class PlantasActivity extends AppCompatActivity {

    private ListView lvPlantas;
    private List<Planta> plantasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantas);

        lvPlantas = findViewById(R.id.lv_plantas);
        plantasList = new ArrayList<>();

        Button btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> finish());

        getPlantasFromWS();
    }

    private void getPlantasFromWS() {
        new Thread() {
            public void run() {
                try {
                    final List<Planta> plantas = PlantaService.getPlantas();
                    runOnUiThread(() -> atualizaLista(plantas));
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(PlantasActivity.this,
                            "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }.start();
    }

    private void atualizaLista(List<Planta> plantas) {
        List<String> nomes = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
                nomes.add(p.getNome() + " (" + p.getEspecie() + ")");
            } else {
                nomes.add(p.getNome());
            }
        }
        lvPlantas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nomes));
    }
}
