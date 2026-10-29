package com.dssmv.plantkeep;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.RegistoDiario;
import com.dssmv.plantkeep.services.PlantaService;

import java.util.ArrayList;
import java.util.List;

/**
 * Ecrã do Diário da Planta: permite reportar o estado de crescimento (texto)
 * e consultar o histórico de registos da planta.
 */
public class DiarioPlantaActivity extends AppCompatActivity {

    private String plantaId;
    private EditText edtRegisto;
    private Button btnAdicionar;
    private TextView txtEstadoDiario;
    private ListView lvDiario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario_planta);

        plantaId = getIntent().getStringExtra("id");
        String nomePlanta = getIntent().getStringExtra("nome");

        TextView txtTitulo = findViewById(R.id.txt_titulo_diario);
        edtRegisto = findViewById(R.id.edt_registo_diario);
        btnAdicionar = findViewById(R.id.btn_adicionar_registo);
        txtEstadoDiario = findViewById(R.id.txt_estado_diario);
        lvDiario = findViewById(R.id.lv_diario);
        Button btnVoltar = findViewById(R.id.btn_voltar_diario);

        if (nomePlanta != null && !nomePlanta.isEmpty()) {
            txtTitulo.setText("Diário: " + nomePlanta);
        }

        btnAdicionar.setOnClickListener(v -> adicionarRegisto());
        btnVoltar.setOnClickListener(v -> finish());

        carregarDiario();
    }

    private void carregarDiario() {
        txtEstadoDiario.setVisibility(View.VISIBLE);
        txtEstadoDiario.setText("A carregar registos...");

        new Thread(() -> {
            try {
                final Planta p = PlantaService.obterPlantaPorId(plantaId);
                runOnUiThread(() -> mostrarRegistos(p != null ? p.getDiario() : null));
            } catch (Exception e) {
                runOnUiThread(() -> txtEstadoDiario.setText("Erro ao carregar diário: " + e.getMessage()));
            }
        }).start();
    }

    private void adicionarRegisto() {
        final String texto = edtRegisto.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Escreve o estado de crescimento da planta.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdicionar.setEnabled(false);

        new Thread(() -> {
            try {
                final Planta pAtualizada = PlantaService.adicionarRegistoDiario(plantaId, texto);
                runOnUiThread(() -> {
                    btnAdicionar.setEnabled(true);
                    edtRegisto.setText("");
                    Toast.makeText(DiarioPlantaActivity.this, "Registo guardado no diário!", Toast.LENGTH_SHORT).show();
                    mostrarRegistos(pAtualizada.getDiario());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAdicionar.setEnabled(true);
                    Toast.makeText(DiarioPlantaActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarRegistos(List<RegistoDiario> registos) {
        if (registos == null || registos.isEmpty()) {
            txtEstadoDiario.setVisibility(View.VISIBLE);
            txtEstadoDiario.setText("Ainda não existem registos no diário desta planta.");
            lvDiario.setAdapter(null);
            return;
        }

        List<RegistoDiario> ordenados = new ArrayList<>(registos);
        // Ordena do registo mais recente para o mais antigo
        ordenados.sort((r1, r2) -> r2.getDataHora().compareTo(r1.getDataHora()));

        txtEstadoDiario.setVisibility(View.GONE);
        lvDiario.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ordenados));
    }
}