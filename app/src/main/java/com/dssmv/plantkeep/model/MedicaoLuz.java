package com.dssmv.plantkeep.model;

public class MedicaoLuz {

    public enum Resultado {
        SEM_REFERENCIA,
        LUZ_INSUFICIENTE,
        LUZ_EXCESSIVA,
        LUZ_ADEQUADA
    }

    private final int maiorLux;

    public MedicaoLuz(int maiorLux) {
        if (maiorLux < 0) {
            throw new IllegalArgumentException("MedicaoLuz: o valor de lux tem de ser >= 0");
        }
        this.maiorLux = maiorLux;
    }

    // Avalia se a luminosidade medida está dentro do intervalo ideal da espécie
    public Resultado avaliar(Integer minLux, Integer maxLux) {
        if (minLux == null && maxLux == null) {
            return Resultado.SEM_REFERENCIA;
        }
        if (minLux != null && maiorLux < minLux) {
            return Resultado.LUZ_INSUFICIENTE;
        }
        if (maxLux != null && maiorLux > maxLux) {
            return Resultado.LUZ_EXCESSIVA;
        }
        return Resultado.LUZ_ADEQUADA;
    }

    @Override
    public String toString() {
        return "MedicaoLuz{maiorLux=" + maiorLux + '}';
    }
}