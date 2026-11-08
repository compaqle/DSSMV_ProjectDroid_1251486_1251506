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

/**
 * Ecrã de medição de luz
 * Faz 3 medições com o sensor de luz
 */
public class MedirLuzActivity extends AppCompatActivity {

    private static final int N_MEDICOES = 3;
    private static final int DURACAO_MEDICAO_MS = 1200;

    private Button btnFazerMedicao;
    private TextView txtResultado;

    private Integer minLux;
    private Integer maxLux;

    private SensorManager sensorManager;
    private SensorEventListener listenerLuz;
    private Handler handler;
    private int medicaoAtual = 0;
    private int maiorLux = 0;
    private boolean aMedir = false;

    private final int[] luxMedidos = new int[N_MEDICOES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medir_luz);

        btnFazerMedicao = findViewById(R.id.btn_fazer_medicao);
        txtResultado = findViewById(R.id.txt_resultado_luz);

        int min = getIntent().getIntExtra("minLux", -1);
        int max = getIntent().getIntExtra("maxLux", -1);
        minLux = (min >= 0) ? min : null;
        maxLux = (max >= 0) ? max : null;

        btnFazerMedicao.setOnClickListener(v -> iniciarMedicao());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && listenerLuz != null) {
            sensorManager.unregisterListener(listenerLuz);
            listenerLuz = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        aMedir = false;
    }

    // Lê o sensor de luz durante 1,2 segundos e guarda o valor máximo registado
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
        final int[] maxLido = {0};

        listenerLuz = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                int valor = (int) event.values[0];
                if (valor > maxLido[0]) {
                    maxLido[0] = valor;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(listenerLuz, luz, SensorManager.SENSOR_DELAY_NORMAL);

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorManager.unregisterListener(listenerLuz);
            listenerLuz = null;
            aMedir = false;

            luxMedidos[medicaoAtual] = maxLido[0];
            if (maxLido[0] > maiorLux) {
                maiorLux = maxLido[0];
            }
            medicaoAtual++;

            if (medicaoAtual >= N_MEDICOES) {
                mostrarResultados();
            } else {
                btnFazerMedicao.setEnabled(true);
                btnFazerMedicao.setText("Fazer Medição " + (medicaoAtual + 1) + "/" + N_MEDICOES);
            }
        }, DURACAO_MEDICAO_MS);
    }

    // Mostra os valores das 3 medições e a avaliação final
    private void mostrarResultados() {
        btnFazerMedicao.setEnabled(false);
        btnFazerMedicao.setText("Medição completa");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < N_MEDICOES; i++) {
            sb.append("Medição ").append(i + 1).append(": ").append(luxMedidos[i]).append(" lux\n");
        }
        sb.append("\n");
        if (minLux != null || maxLux != null) {
            sb.append("Ideal: ").append(formatarIntervalo()).append(" lux\n");
        }

        MedicaoLuz medicao = new MedicaoLuz(maiorLux);
        sb.append(obterTextoAvaliacao(medicao.avaliar(minLux, maxLux)));

        txtResultado.setText(sb.toString());
        txtResultado.setVisibility(View.VISIBLE);
    }

    private String obterTextoAvaliacao(MedicaoLuz.Resultado resultado) {
        if (resultado == MedicaoLuz.Resultado.LUZ_INSUFICIENTE) {
            return "Luz a menos — este local está MUITO ESCURO para a planta (MAU).";
        }
        if (resultado == MedicaoLuz.Resultado.LUZ_EXCESSIVA) {
            return "Luz a mais — o sol aqui é forte demais para a planta (MAU).";
        }
        if (resultado == MedicaoLuz.Resultado.LUZ_ADEQUADA) {
            return "Luz adequada — este local é BOM para a planta.";
        }
        return "Sem intervalo ideal de lux conhecido para esta espécie.";
    }

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