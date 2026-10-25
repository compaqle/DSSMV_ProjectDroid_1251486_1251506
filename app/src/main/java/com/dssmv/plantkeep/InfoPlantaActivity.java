package com.dssmv.plantkeep;

import android.graphics.Bitmap;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_planta);

        llInfo = findViewById(R.id.ll_info);
        imgInfo = findViewById(R.id.img_info);
        TextView titulo = findViewById(R.id.txt_titulo);
        Button btnApagar = findViewById(R.id.btn_apagar);
        btnApagar.setOnClickListener(v -> confirmarApagar());

        id = getIntent().getStringExtra("id");
        nome = getIntent().getStringExtra("nome");
        String especie = getIntent().getStringExtra("especie");
        pid = getIntent().getStringExtra("pid");

        titulo.setText(especie == null || especie.isEmpty() ? nome : nome + " (" + especie + ")");

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
        // imagem (só se existir)
        final String url = d.getImageUrl();
        if (url != null && !url.isEmpty()) {
            new Thread(() -> {
                final Bitmap b = carregarImagem(url);
                if (b != null) {
                    imgInfo.post(() -> imgInfo.setImageBitmap(b));
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

    private static Bitmap carregarImagem(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            InputStream in = c.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            return null;
        }
    }
}
