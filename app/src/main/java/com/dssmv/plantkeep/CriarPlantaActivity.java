package com.dssmv.plantkeep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.model.Especie;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantBookService;
import com.dssmv.plantkeep.services.PlantaService;

import java.util.List;

// Ecrã que permite pesquisar a espécie e guardar uma nova planta
public class CriarPlantaActivity extends AppCompatActivity {

    private EditText edtPesquisa;
    private EditText edtNome;
    private Button btnPesquisar;
    private Button btnGuardar;
    private ListView lvResultados;
    private TextView lblResultado;
    private TextView txtEscolhida;

    private List<Especie> lista;   // resultados da pesquisa
    private Especie escolhida;     // espécie que o utilizador escolheu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_planta);

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
                final List<Especie> l = PlantBookService.pesquisar(termo);
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

    private void mostrarResultados(List<Especie> l) {
        btnPesquisar.setEnabled(true);
        if (l == null || l.isEmpty()) {
            Toast.makeText(this, "Não encontrei nada com esse nome. Experimenta o nome científico.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        lista = l;
        lvResultados.setAdapter(new ArrayAdapter<Especie>(this, 0, lista) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Especie e = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(CriarPlantaActivity.this)
                            .inflate(R.layout.item_especie, parent, false);
                }
                TextView txt = convertView.findViewById(R.id.txt_especie);
                if (e != null) {
                    txt.setText(e.getNomeFormatado());
                }
                return convertView;
            }
        });

        // Passa da pesquisa para a lista de resultados
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

        // Passa dos resultados para a introdução do nome/apelido
        lblResultado.setVisibility(View.GONE);
        lvResultados.setVisibility(View.GONE);
        txtEscolhida.setText("Espécie: " + escolhida.getNomeCientifico());
        txtEscolhida.setVisibility(View.VISIBLE);
        edtNome.setVisibility(View.VISIBLE);
        btnGuardar.setVisibility(View.VISIBLE);
    }

    private void guardar() {
        if (escolhida == null) {
            return;
        }
        final String nome = edtNome.getText().toString().trim();
        final String especie = escolhida.getNomeCientifico();
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