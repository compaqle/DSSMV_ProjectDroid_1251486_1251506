package com.dssmv.plantkeep;

import android.content.Intent;
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
 * Ecrã que mostra a lista de plantas. Tocar numa planta abre a sua informação.
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

        // Tocar numa planta abre a sua informação
        lvPlantas.setOnItemClickListener((parent, view, position, id) ->
                abrirInfo(plantasList.get(position)));
    }

    // recarrega sempre que se volta para este ecrã (ex. após apagar uma planta)
    @Override
    protected void onResume() {
        super.onResume();
        getPlantasFromWS();
    }

    private void abrirInfo(Planta p) {
        Intent i = new Intent(this, InfoPlantaActivity.class);
        i.putExtra("id", p.getId());
        i.putExtra("nome", p.getNome());
        i.putExtra("especie", p.getEspecie());
        i.putExtra("pid", p.getPid());
        startActivity(i);
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
        plantasList.clear();
        plantasList.addAll(plantas);
        List<String> nomes = new ArrayList<>();
        for (Planta p : plantas) {
            nomes.add(nomePlanta(p));
        }
        lvPlantas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nomes));
    }

    private String nomePlanta(Planta p) {
        if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
            return p.getNome() + " (" + p.getEspecie() + ")";
        }
        return p.getNome();
    }
}
