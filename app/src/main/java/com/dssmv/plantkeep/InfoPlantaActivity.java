package com.dssmv.plantkeep;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dssmv.plantkeep.model.CuidadosEspecie;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantBookService;
import com.dssmv.plantkeep.services.PlantaService;

/**
 * Ecrã da informação da planta
 * Permite ver os detalhes de uma planta e aceder às funcionalidades da planta
 */
public class InfoPlantaActivity extends AppCompatActivity {

    private LinearLayout llInfo;
    private ImageView imgInfo;

    private String id;
    private String pid;
    private String nome;

    private Integer minLux;
    private Integer maxLux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_planta);

        llInfo = findViewById(R.id.ll_info);
        imgInfo = findViewById(R.id.img_info);
        TextView titulo = findViewById(R.id.txt_titulo);
        Button btnDiario = findViewById(R.id.btn_diario);
        Button btnApagar = findViewById(R.id.btn_apagar);
        Button btnVerificarLuz = findViewById(R.id.btn_verificar_luz);
        Button btnRega = findViewById(R.id.btn_rega);

        id = getIntent().getStringExtra("id");
        nome = getIntent().getStringExtra("nome");
        String especie = getIntent().getStringExtra("especie");
        pid = getIntent().getStringExtra("pid");

        titulo.setText(especie != null && !especie.isEmpty() ? nome + " (" + especie + ")" : nome);

        btnDiario.setOnClickListener(v -> abrirDiario());
        btnApagar.setOnClickListener(v -> confirmarApagar());
        btnVerificarLuz.setOnClickListener(v -> abrirVerificacaoLuz());
        btnRega.setOnClickListener(v -> abrirRega());

        // Ajusta margens para não sobrepor as barras do sistema
        final LinearLayout llRoot = findViewById(R.id.ll_root);
        final int paddingBase = Math.round(24 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(llRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(paddingBase + bars.left, paddingBase + bars.top,
                    paddingBase + bars.right, paddingBase + bars.bottom);
            return insets;
        });

        carregarDados();
    }

    private void abrirDiario() {
        Intent i = new Intent(this, DiarioPlantaActivity.class);
        i.putExtra("id", id);
        i.putExtra("nome", nome);
        startActivity(i);
    }

    private void abrirRega() {
        Intent i = new Intent(this, RegaPlantaActivity.class);
        i.putExtra("id", id);
        i.putExtra("nome", nome);
        startActivity(i);
    }

    private void abrirVerificacaoLuz() {
        Intent i = new Intent(this, MedirLuzActivity.class);
        i.putExtra("minLux", minLux != null ? minLux : -1);
        i.putExtra("maxLux", maxLux != null ? maxLux : -1);
        startActivity(i);
    }

    private void confirmarApagar() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar planta")
                .setMessage("Tens a certeza que queres apagar \"" + nome + "\"?")
                .setPositiveButton("Apagar", (d, w) -> apagar())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void apagar() {
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Sem id para apagar", Toast.LENGTH_SHORT).show();
            return;
        }
        final Planta p = new Planta(nome != null ? nome : "planta", "");
        p.setId(id);

        new Thread(() -> {
            try {
                PlantaService.apagar(p);
                runOnUiThread(() -> {
                    Toast.makeText(InfoPlantaActivity.this, "Planta apagada!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(InfoPlantaActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void carregarDados() {
        if (pid == null || pid.isEmpty()) {
            Toast.makeText(this, "Sem espécie associada (pid vazio)", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                final CuidadosEspecie cuidados = PlantBookService.detalhe(pid);
                runOnUiThread(() -> mostrar(cuidados));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(InfoPlantaActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrar(CuidadosEspecie cuidados) {
        if (cuidados == null) {
            return;
        }
        minLux = cuidados.getMinLightLux();
        maxLux = cuidados.getMaxLightLux();

        final String url = cuidados.getImagemUrl();
        if (url != null && !url.isEmpty()) {
            new Thread(() -> {
                final byte[] b = PlantBookService.obterImagemBytes(url);
                if (b != null) {
                    imgInfo.post(() -> imgInfo.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length)));
                }
            }).start();
        } else {
            imgInfo.setVisibility(View.GONE);
        }

        adicionarLinha("Temperatura", intervalo(cuidados.getMinTemp(), cuidados.getMaxTemp(), " °C"));
        adicionarLinha("Humidade do ar", intervalo(cuidados.getMinEnvHumid(), cuidados.getMaxEnvHumid(), " %"));
        adicionarLinha("Humidade do solo", intervalo(cuidados.getMinSoilMoist(), cuidados.getMaxSoilMoist(), " %"));
        adicionarLinha("Luz", intervalo(cuidados.getMinLightLux(), cuidados.getMaxLightLux(), " lux"));
        adicionarLinha("Condutividade do solo", intervalo(cuidados.getMinSoilEc(), cuidados.getMaxSoilEc(), " µS/cm"));
        adicionarLinha("Categoria", cuidados.getCategoria());
        adicionarLinha("Origem", cuidados.getOrigem());
    }

    // Adiciona uma linha de texto ao ecrã apenas se o valor não estiver vazio
    private void adicionarLinha(String rotulo, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return;
        }
        TextView row = new TextView(this);
        row.setTextSize(16);
        row.setPadding(0, 12, 0, 0);
        row.setText(rotulo + ":  " + valor);
        llInfo.addView(row);
    }

    // Formata um intervalo "min - max" com a respetiva unidade
    private String intervalo(Integer min, Integer max, String unidade) {
        if (min == null && max == null) {
            return "";
        }
        if (min == null) {
            return max + unidade;
        }
        if (max == null) {
            return min + unidade;
        }
        return min + " - " + max + unidade;
    }
}