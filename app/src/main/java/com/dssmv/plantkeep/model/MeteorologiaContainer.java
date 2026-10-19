package com.dssmv.plantkeep.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MeteorologiaContainer {

    private List<Meteorologia> previsaoHoraria;

    public MeteorologiaContainer(List<Meteorologia> previsaoHoraria) {
        this.previsaoHoraria = previsaoHoraria;
    }

    public List<Meteorologia> getPrevisaoHoraria() {
        return previsaoHoraria;
    }

    //Calcula o total de precipitação em um dia (mm). Caso não hajam dados meteorologicos guardados retorna -1
    public double calcularPrecipitacaoPorDia(Date diaEscolhido) {
        if (diaEscolhido == null || previsaoHoraria == null || previsaoHoraria.isEmpty()) {
            return -1;
        }

        double total = 0;
        boolean encontrouDados = false;

        Calendar calEscolhido = Calendar.getInstance();
        calEscolhido.setTime(diaEscolhido);
        Calendar calRegisto = Calendar.getInstance();

        for (Meteorologia registo : previsaoHoraria) {
            calRegisto.setTime(registo.getDataHora());

            if (calEscolhido.get(Calendar.DAY_OF_YEAR) == calRegisto.get(Calendar.DAY_OF_YEAR)) {
                total += registo.getPrecipitacao();
                encontrouDados = true;
            }
        }

        if (encontrouDados) {
            return total;
        } else {
            return -1;
        }
    }
}