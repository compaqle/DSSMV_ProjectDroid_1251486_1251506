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

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantBookService;
import com.dssmv.plantkeep.services.PlantaService;

/**
 * Ecrã com as informações relevantes de uma planta (vindas do OpenPlantbook),
 * apresentadas quase em bruto: imagem + valores dos cuidados.
 */
public class InfoPlantaActivity extends AppCompatActivity {

    private LinearLayout llInfo;
    private ImageView imgInfo;
    private String id;
    private String pid;
    private String nome;

    // limites de luz (lux) do ideal da espécie, para a verificação de luz
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

        String textoTitulo;
        if (especie != null && !especie.isEmpty()) {
            textoTitulo = nome + " (" + especie + ")";
        } else {
            textoTitulo = nome;
        }
        titulo.setText(textoTitulo);

        btnDiario.setOnClickListener(v -> abrirDiario());
        btnApagar.setOnClickListener(v -> confirmarApagar());
        btnVerificarLuz.setOnClickListener(v -> abrirVerificacaoLuz());
        btnRega.setOnClickListener(v -> abrirRega());

        // edge-to-edge: afasta o conteúdo das barras do sistema (banner em cima, nav em baixo)
        final LinearLayout llRoot = findViewById(R.id.ll_root);
        final int paddingBase = Math.round(24 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(llRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(paddingBase + bars.left,
                    paddingBase + bars.top,
                    paddingBase + bars.right,
                    paddingBase + bars.bottom);
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
        int valorMinLux;
        if (minLux != null) {
            valorMinLux = minLux;
        } else {
            valorMinLux = -1;
        }
        i.putExtra("minLux", valorMinLux);
        int valorMaxLux;
        if (maxLux != null) {
            valorMaxLux = maxLux;
        } else {
            valorMaxLux = -1;
        }
        i.putExtra("maxLux", valorMaxLux);
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
        String nomePlanta;
        if (nome != null) {
            nomePlanta = nome;
        } else {
            nomePlanta = "planta";
        }
        final Planta p = new Planta(nomePlanta, "");
        p.setId(id);

        new Thread(() -> {
            try {
                PlantaService.apagar(p);
                runOnUiThread(() -> {
                    Toast.makeText(InfoPlantaActivity.this,
                            "Planta apagada!", Toast.LENGTH_SHORT).show();
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
                final DetalheOpb d = PlantBookService.detalhe(pid);
                runOnUiThread(() -> mostrar(d));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(InfoPlantaActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrar(DetalheOpb d) {
        // guarda os limites de luz para a verificação de luz
        minLux = d.getMinLightLux();
        maxLux = d.getMaxLightLux();

        // imagem (só se existir)
        final String url = d.getImageUrl();
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

        // dados crus: só as linhas que têm valor
        adicionarLinha("Temperatura", intervalo(d.getMinTemp(), d.getMaxTemp()) + " °C");
        adicionarLinha("Humidade do ar", intervalo(d.getMinEnvHumid(), d.getMaxEnvHumid()) + " %");
        adicionarLinha("Humidade do solo", intervalo(d.getMinSoilMoist(), d.getMaxSoilMoist()) + " %");
        adicionarLinha("Luz", intervalo(d.getMinLightLux(), d.getMaxLightLux()) + " lux");
        adicionarLinha("Condutividade do solo", intervalo(d.getMinSoilEc(), d.getMaxSoilEc()) + " µS/cm");
        adicionarLinha("Categoria", d.getCategory());
        adicionarLinha("Origem", d.getOrigin());
    }

    /** Adiciona uma linha "rotulo: valor" se o valor não for vazio. */
    private void adicionarLinha(String rotulo, String valor) {
        if (valor == null || valor.trim().isEmpty() || valor.startsWith("null")) {
            return;
        }
        TextView row = new TextView(this);
        row.setTextSize(16);
        row.setPadding(0, 12, 0, 0);
        row.setText(rotulo + ":  " + valor);
        llInfo.addView(row);
    }

    /** Formata um intervalo "min - max" (ou só o valor, se só um existir). */
    private String intervalo(Integer min, Integer max) {
        if (min == null && max == null) {
            return "";
        }
        if (min == null) {
            return String.valueOf(max);
        }
        if (max == null) {
            return String.valueOf(min);
        }
        return min + " - " + max;
    }
}