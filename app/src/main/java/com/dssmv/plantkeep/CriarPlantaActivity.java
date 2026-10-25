package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantaService;

/**
 * Ecrã de criar planta (o formulario)
 */
public class CriarPlantaActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEspecie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtNome = findViewById(R.id.edt_nome);
        edtEspecie = findViewById(R.id.edt_especie);

        Button btnSalvar = findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(v -> criar());
    }

    private void criar() {
        final String nome = edtNome.getText().toString().trim();
        final String especie = edtEspecie.getText().toString().trim();
        new Thread() {
            public void run() {
                try {
                    PlantaService.criar(new Planta(nome, especie));
                    runOnUiThread(() -> {
                        Toast.makeText(CriarPlantaActivity.this,
                                "Planta guardada!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(CriarPlantaActivity.this,
                            "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }.start();
    }

    private void limparCampos() {
        edtNome.setText("");
        edtEspecie.setText("");
    }
}
