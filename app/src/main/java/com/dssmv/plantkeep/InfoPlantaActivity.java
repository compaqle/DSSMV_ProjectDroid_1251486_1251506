package com.dssmv.plantkeep;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.services.PlantBookService;

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
    private String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_planta);

        llInfo = findViewById(R.id.ll_info);
        imgInfo = findViewById(R.id.img_info);
        TextView titulo = findViewById(R.id.txt_titulo);

        String nome = getIntent().getStringExtra("nome");
        String especie = getIntent().getStringExtra("especie");
        pid = getIntent().getStringExtra("pid");

        titulo.setText(especie == null || especie.isEmpty() ? nome : nome + " (" + especie + ")");

        carregarDados();
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
