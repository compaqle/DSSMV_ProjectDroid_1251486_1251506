package com.dssmv.plantkeep;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private Button btnApagar;
    private int selecionada = -1;   // posição da planta escolhida na lista (-1 = nenhuma)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantas);

        lvPlantas = findViewById(R.id.lv_plantas);
        plantasList = new ArrayList<>();

        btnApagar = findViewById(R.id.btn_apagar);
        btnApagar.setOnClickListener(v -> confirmarApagar());

        Button btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> finish());

        // Tocar numa planta escolhe-a (fica acesa a possibilidade de a apagar)
        lvPlantas.setOnItemClickListener((parent, view, position, id) -> {
            selecionada = position;
            btnApagar.setEnabled(true);
            Toast.makeText(this, "Selecionada: " + nomePlanta(plantasList.get(position)),
                    Toast.LENGTH_SHORT).show();
        });

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
        plantasList.clear();
        plantasList.addAll(plantas);
        List<String> nomes = new ArrayList<>();
        for (Planta p : plantas) {
            nomes.add(nomePlanta(p));
        }
        lvPlantas.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nomes));
        selecionada = -1;
        btnApagar.setEnabled(false);
    }

    private String nomePlanta(Planta p) {
        if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
            return p.getNome() + " (" + p.getEspecie() + ")";
        }
        return p.getNome();
    }

    private void confirmarApagar() {
        if (selecionada < 0 || selecionada >= plantasList.size()) {
            return;
        }
        final Planta apagar = plantasList.get(selecionada);
        new AlertDialog.Builder(this)
                .setTitle("Apagar planta")
                .setMessage("Tens a certeza que queres apagar \"" + apagar.getNome() + "\"?")
                .setPositiveButton("Apagar", (d, w) -> apagarNoServidor(apagar))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void apagarNoServidor(final Planta p) {
        new Thread() {
            public void run() {
                try {
                    PlantaService.apagar(p);
                    runOnUiThread(() -> {
                        Toast.makeText(PlantasActivity.this,
                                "Planta apagada!", Toast.LENGTH_SHORT).show();
                        getPlantasFromWS();   // recarrega a lista
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(PlantasActivity.this,
                            "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }.start();
    }
}
