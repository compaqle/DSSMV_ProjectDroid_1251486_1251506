package com.dssmv.plantkeep;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.model.MedicaoLuz;

import java.util.ArrayList;
import java.util.List;

/**
 * Ecrã de verificação de luz da planta.
 * O utilizador carrega 3 vezes no botão e faz 3 medições de lux no local;
 * no fim mostram-se os valores medidos e se a luz é boa ou má para a espécie.
 */
public class MedirLuzActivity extends AppCompatActivity {

    private static final int N_MEDICOES = 3;
    private static final int DURACAO_MEDICAO_MS = 1200;

    private Button btnFazerMedicao;
    private TextView txtResultado;

    // limites de lux (lux) do ideal da espécie (-1 = desconhecido)
    private Integer minLux = -1;
    private Integer maxLux = -1;

    private SensorManager sensorManager;
    private SensorEventListener listenerLuz;
    private Handler handler;
    private float maxRange;

    private int medicaoAtual = 0;
    private boolean aMedir = false;
    private final int[] luxMedidos = new int[N_MEDICOES];
    private final boolean[] saturados = new boolean[N_MEDICOES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medir_luz);

        btnFazerMedicao = findViewById(R.id.btn_fazer_medicao);
        txtResultado = findViewById(R.id.txt_resultado_luz);

        int min = getIntent().getIntExtra("minLux", -1);
        int max = getIntent().getIntExtra("maxLux", -1);
        minLux = (min > 0) ? min : null;
        maxLux = (max > 0) ? max : null;

        btnFazerMedicao.setOnClickListener(v -> iniciarMedicao());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // desliga o sensor se sair do ecrã a meio de uma medição
        if (sensorManager != null && listenerLuz != null) {
            sensorManager.unregisterListener(listenerLuz);
            listenerLuz = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        aMedir = false;
    }

    /**
     * Mede o lux uma vez: regista o sensor de luz durante ~1,2 s, guarda o valor
     * máximo e trata a saturação ("≥ máximo" em vez de inventar um lux).
     */
    private void iniciarMedicao() {
        if (aMedir) {
            return;
        }
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor luz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (luz == null) {
            txtResultado.setVisibility(View.VISIBLE);
            txtResultado.setText("Este telemóvel não tem sensor de luz.");
            btnFazerMedicao.setEnabled(false);
            return;
        }

        aMedir = true;
        btnFazerMedicao.setEnabled(false);
        maxRange = luz.getMaximumRange();
        final int[] maxLido = {0};
        final boolean[] saturado = {false};

        listenerLuz = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float valor = event.values[0];
                if (valor >= maxRange) {
                    saturado[0] = true;
                }
                if (valor > maxLido[0]) {
                    maxLido[0] = (int) valor;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // sem uso
            }
        };
        sensorManager.registerListener(listenerLuz, luz, SensorManager.SENSOR_DELAY_NORMAL);

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorManager.unregisterListener(listenerLuz);
            listenerLuz = null;
            aMedir = false;

            // se saturou (luz >= teto do sensor), guarda o teto — a luz real é ">="
            luxMedidos[medicaoAtual] = saturado[0] ? (int) maxRange : maxLido[0];
            saturados[medicaoAtual] = saturado[0];
            medicaoAtual++;

            if (medicaoAtual >= N_MEDICOES) {
                mostrarResultados();
            } else {
                btnFazerMedicao.setEnabled(true);
                btnFazerMedicao.setText("Fazer Medição " + (medicaoAtual + 1) + "/" + N_MEDICOES);
            }
        }, DURACAO_MEDICAO_MS);
    }

    /** Mostra as 3 medições e o veredito (bom/mau) vindo do modelo de domínio. */
    private void mostrarResultados() {
        btnFazerMedicao.setEnabled(false);
        btnFazerMedicao.setText("Medição completa");

        // recolhe as leituras e constrói o modelo de domínio
        List<Integer> luxList = new ArrayList<>();
        List<Boolean> satList = new ArrayList<>();
        for (int i = 0; i < N_MEDICOES; i++) {
            luxList.add(luxMedidos[i]);
            satList.add(saturados[i]);
        }
        MedicaoLuz medicao = new MedicaoLuz(luxList, satList);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < N_MEDICOES; i++) {
            String valor = medicao.estaSaturada(i)
                    ? "≥ " + medicao.getLux(i)
                    : String.valueOf(medicao.getLux(i));
            sb.append("Medição ").append(i + 1).append(": ").append(valor).append(" lux\n");
        }

        sb.append("\n");
        if (minLux != null || maxLux != null) {
            sb.append("Ideal: ").append(formatarIntervalo()).append(" lux\n");
        }
        // a regra de decisão (bom/mau) vive no modelo
        sb.append(medicao.avaliar(minLux, maxLux));

        txtResultado.setText(sb.toString());
        txtResultado.setVisibility(View.VISIBLE);
    }

    /** Formata o intervalo ideal "min - max" (só apresentação). */
    private String formatarIntervalo() {
        if (minLux != null && maxLux != null) {
            return minLux + " - " + maxLux;
        }
        if (minLux != null) {
            return "mínimo " + minLux;
        }
        if (maxLux != null) {
            return "máximo " + maxLux;
        }
        return "";
    }
}
