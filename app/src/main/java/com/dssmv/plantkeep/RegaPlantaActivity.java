package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.AvisoService;
import com.dssmv.plantkeep.services.PlantaService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ecrã de rega
 * Mostra a última e a próxima rega e permite regar a planta
 */
public class RegaPlantaActivity extends AppCompatActivity {

    private String id;
    private Planta plantaAtual;
    private TextView txtUltimaRega;
    private TextView txtProximaRega;
    private TextView txtEstado;
    private Button btnRegar;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rega_planta);

        id = getIntent().getStringExtra("id");
        String nome = getIntent().getStringExtra("nome");

        TextView txtTitulo = findViewById(R.id.txt_titulo_rega);
        txtUltimaRega = findViewById(R.id.txt_ultima_rega);
        txtProximaRega = findViewById(R.id.txt_proxima_rega);
        txtEstado = findViewById(R.id.txt_estado_rega);
        btnRegar = findViewById(R.id.btn_regar_planta);
        Button btnVoltar = findViewById(R.id.btn_voltar_rega);

        txtTitulo.setText(nome != null && !nome.isEmpty() ? "Rega de " + nome : "Rega da Planta");

        btnRegar.setOnClickListener(v -> regar());
        btnVoltar.setOnClickListener(v -> finish());

        // Ajusta margens para não sobrepor as barras do sistema
        final LinearLayout llRoot = findViewById(R.id.ll_root_rega);
        final int paddingBase = Math.round(24 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(llRoot, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(paddingBase + bars.left, paddingBase + bars.top,
                    paddingBase + bars.right, paddingBase + bars.bottom);
            return insets;
        });

        carregar();
    }

    private void carregar() {
        txtEstado.setText("A carregar dados de rega...");
        btnRegar.setEnabled(false);

        new Thread(() -> {
            try {
                final Planta p = PlantaService.obterPlantaPorId(id);
                final Aviso proxima = AvisoService.obterProximaRega(p);
                runOnUiThread(() -> mostrar(p, proxima));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegar.setEnabled(true);
                    txtEstado.setText("Erro ao carregar dados de rega.");
                    Toast.makeText(RegaPlantaActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrar(Planta p, Aviso proxima) {
        plantaAtual = p;
        if (p == null) {
            txtEstado.setText("Planta não encontrada.");
            btnRegar.setEnabled(false);
            return;
        }
        btnRegar.setEnabled(true);

        if (p.getUltimaRega() > 0) {
            txtUltimaRega.setText("Última rega: " + sdf.format(new Date(p.getUltimaRega())));
        } else {
            txtUltimaRega.setText("Última rega: nunca foi regada.");
        }

        if (proxima != null && proxima.getDataHora() != null) {
            txtProximaRega.setText("Próxima rega: " + proxima.formatarData());
        } else {
            txtProximaRega.setText("Próxima rega: sem necessidade nos próximos dias.");
        }
        txtEstado.setText("");
    }

    private void regar() {
        if (plantaAtual == null) {
            return;
        }
        btnRegar.setEnabled(false);
        txtEstado.setText("A guardar rega...");

        new Thread(() -> {
            try {
                PlantaService.regar(plantaAtual);
                final Aviso proxima = AvisoService.obterProximaRega(plantaAtual);
                runOnUiThread(() -> {
                    Toast.makeText(RegaPlantaActivity.this, "Planta marcada como regada!", Toast.LENGTH_SHORT).show();
                    mostrar(plantaAtual, proxima);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegar.setEnabled(true);
                    txtEstado.setText("");
                    Toast.makeText(RegaPlantaActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}