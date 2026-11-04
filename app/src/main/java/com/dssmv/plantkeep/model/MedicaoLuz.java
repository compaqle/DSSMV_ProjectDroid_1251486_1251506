package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/** Modelo de domínio da medição de luz de uma planta. */
public class MedicaoLuz {

    /** Leituras de lux, uma por medição. Uma leitura saturada guarda o máximo
     * que o sensor alcançou (a luz real é ">=" a esse valor). */
    private final List<Integer> lux;
    private final List<Boolean> saturado;

    public MedicaoLuz(List<Integer> lux, List<Boolean> saturado) {
        if (lux == null || saturado == null) {
            throw new IllegalArgumentException("MedicaoLuz: listas não podem ser nulas");
        }
        if (lux.size() != saturado.size() || lux.isEmpty()) {
            throw new IllegalArgumentException("MedicaoLuz: é precisa pelo menos uma leitura e uma flag saturado por leitura");
        }
        this.lux = new ArrayList<>(lux);
        this.saturado = new ArrayList<>(saturado);

        for (Integer v : lux) {
            if (v == null || v < 0) {
                throw new IllegalArgumentException("MedicaoLuz: as leituras têm de ser >= 0");
            }
        }
        for (Boolean s : saturado) {
            if (s == null) {
                throw new IllegalArgumentException("MedicaoLuz: a flag saturado não pode ser nula");
            }
        }
    }

    public int getLux(int i) {
        return lux.get(i);
    }

    public boolean estaSaturada(int i) {
        return saturado.get(i);
    }

    /** Melhor leitura: o maior lux observado (protocolo das 3 medições) */
    public int melhorLeitura() {
        int max = 0;
        for (int v = 0; v < lux.size(); v++) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /**
     * Regra de decisão: avalia se o local tem luz boa/má para a espécie,
     * comparando a melhor leitura com o intervalo ideal (minLux/maxLux).
     */
    public String avaliar(Integer minLux, Integer maxLux) {
        if (minLux == null && maxLux == null) {
            return "Sem intervalo ideal de lux conhecido para esta espécie.";
        }
        int medida = melhorLeitura();
        if (minLux != null && medida < minLux) {
            return "Luz a menos — este local está MUITO ESCURO para a planta (MAU).";
        }
        if (maxLux != null && medida > maxLux) {
            return "Luz a mais — o sol aqui é forte demais para a planta (MAU).";
        }
        return "Luz adequada — este local é BOM para a planta.";
    }

    @NonNull
    @Override
    public String toString() {
        return "MedicaoLuz{" +
                "lux=" + lux +
                ", saturado=" + saturado +
                '}';
    }
}
