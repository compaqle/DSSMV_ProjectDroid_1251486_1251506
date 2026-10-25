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

import com.dssmv.plantkeep.dto.EspecieOpb;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantBookService;
import com.dssmv.plantkeep.services.PlantaService;

import java.util.ArrayList;
import java.util.List;

/**
 * Ecrã de criar planta.
 * Fluxo: pesquisar espécie no OpenPlantbook -> escolher a espécie -> dar um nome/aplido -> guardar.
 */
public class CriarPlantaActivity extends AppCompatActivity {

    private EditText edtPesquisa;
    private EditText edtNome;
    private Button btnPesquisar;
    private Button btnGuardar;
    private ListView lvResultados;
    private TextView lblResultado;
    private TextView txtEscolhida;

    private List<EspecieOpb> lista;   // resultados da pesquisa
    private EspecieOpb escolhida;     // espécie que o utilizador escolheu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtPesquisa = findViewById(R.id.edt_pesquisa);
        edtNome = findViewById(R.id.edt_nome);
        btnPesquisar = findViewById(R.id.btn_pesquisar);
        btnGuardar = findViewById(R.id.btn_guardar);
        lvResultados = findViewById(R.id.lv_resultados);
        lblResultado = findViewById(R.id.lbl_resultado);
        txtEscolhida = findViewById(R.id.txt_escolhida);

        btnPesquisar.setOnClickListener(v -> pesquisar());
        lvResultados.setOnItemClickListener((parent, view, position, id) -> escolher(position));
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void pesquisar() {
        final String termo = edtPesquisa.getText().toString().trim();
        if (termo.isEmpty()) {
            Toast.makeText(this, "Escreve o nome da espécie", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPesquisar.setEnabled(false);
        new Thread(() -> {
            try {
                final List<EspecieOpb> l = PlantBookService.pesquisar(termo);
                runOnUiThread(() -> mostrarResultados(l));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnPesquisar.setEnabled(true);
                    Toast.makeText(CriarPlantaActivity.this,
                            "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarResultados(List<EspecieOpb> l) {
        btnPesquisar.setEnabled(true);
        if (l == null || l.isEmpty()) {
            Toast.makeText(this, "Nada encontrado no OpenPlantbook", Toast.LENGTH_SHORT).show();
            return;
        }
        lista = l;

        List<String> nomes = new ArrayList<>();
        for (EspecieOpb e : l) {
            String texto = e.getDisplayPid() == null || e.getDisplayPid().isEmpty()
                    ? e.getPid() : e.getDisplayPid();
            if (e.getAlias() != null && !e.getAlias().isEmpty()
                    && !e.getAlias().equalsIgnoreCase(texto)) {
                texto = texto + " (" + e.getAlias() + ")";
            }
            nomes.add(texto);
        }

        lvResultados.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, nomes));

        // passa de "pesquisa" para "resultados"
        edtPesquisa.setVisibility(View.GONE);
        btnPesquisar.setVisibility(View.GONE);
        lblResultado.setVisibility(View.VISIBLE);
        lvResultados.setVisibility(View.VISIBLE);
    }

    private void escolher(int position) {
        if (lista == null || position < 0 || position >= lista.size()) {
            return;
        }
        escolhida = lista.get(position);

        // passa de "resultados" para "escolha + nome/apelido"
        lblResultado.setVisibility(View.GONE);
        lvResultados.setVisibility(View.GONE);
        txtEscolhida.setText("Espécie: " + escolhida.getDisplayPid());
        txtEscolhida.setVisibility(View.VISIBLE);
        edtNome.setVisibility(View.VISIBLE);
        btnGuardar.setVisibility(View.VISIBLE);
    }

    private void guardar() {
        if (escolhida == null) {
            return;
        }
        final String nome = edtNome.getText().toString().trim();
        final String especie = escolhida.getDisplayPid();
        final String pid = escolhida.getPid();

        new Thread(() -> {
            try {
                Planta p = new Planta(nome, especie);
                p.setPid(pid);
                PlantaService.criar(p);
                runOnUiThread(() -> {
                    Toast.makeText(CriarPlantaActivity.this,
                            "Planta guardada!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(CriarPlantaActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
