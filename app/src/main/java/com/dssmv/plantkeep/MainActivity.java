package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements PlanView {

    private EditText edtNome;
    private EditText edtEspecie;
    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtNome = findViewById(R.id.edt_nome);
        edtEspecie = findViewById(R.id.edt_especie);

        PlantaContainer model = new PlantaContainer();   // 1. cria o MODEL
        controller = new Controller(model, this);        // 2. cria o CONTROLLER (com model + view)
        controller.run();                                 // 3. manda o controller trabalhar

        Button btnSalvar = findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(v -> controller.criar());

        Button btnListar = findViewById(R.id.btn_listar);
        btnListar.setOnClickListener(v -> controller.listar());
    }

    @Override
    public Planta getPlantaInput() {
        String nome = edtNome.getText().toString().trim();
        String especie = edtEspecie.getText().toString().trim();
        return new Planta(nome, especie);
    }

    @Override
    public void mostraMensagem(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void atualizaLista(List<Planta> plantas) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            for (Planta p : plantas) {
                sb.append(p.getNome());
                if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
                    sb.append(" (").append(p.getEspecie()).append(")");
                }
                sb.append("\n");
            }
            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void limparCampos() {
        runOnUiThread(() -> {
            edtNome.setText("");
            edtEspecie.setText("");
        });
    }
}
