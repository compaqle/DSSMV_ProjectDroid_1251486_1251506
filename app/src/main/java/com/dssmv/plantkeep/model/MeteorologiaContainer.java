package com.dssmv.plantkeep.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MeteorologiaContainer {

    private List<Meteorologia> previsaoHoraria;
    private static MeteorologiaContainer instancia;

    private MeteorologiaContainer() {
        this.previsaoHoraria = new ArrayList<>();
    }

    public static synchronized MeteorologiaContainer getInstance() {
        if (instancia == null) {
            instancia = new MeteorologiaContainer();
        }
        return instancia;
    }

    public List<Meteorologia> getPrevisaoHoraria() {
        return previsaoHoraria;
    }

    public void setPrevisaoHoraria(List<Meteorologia> previsaoHoraria) {
        this.previsaoHoraria = (previsaoHoraria != null) ? previsaoHoraria : new ArrayList<>();
    }

    /** Devolve o registo (com temperatura e data/hora) mais frio previsto daqui para a frente. */
    public Meteorologia obterRegistoTemperaturaMinimaPrevista() {
        if (previsaoHoraria.isEmpty()) {
            return null;
        }
        Date agora = new Date();
        Meteorologia registoMin = null;

        for (Meteorologia registo : previsaoHoraria) {
            if (!registo.getDataHora().before(agora)) {
                if (registoMin == null || registo.getTemperatura() < registoMin.getTemperatura()) {
                    registoMin = registo;
                }
            }
        }
        return registoMin;
    }

    /** Devolve o registo (com temperatura e data/hora) mais quente previsto daqui para a frente. */
    public Meteorologia obterRegistoTemperaturaMaximaPrevista() {
        if (previsaoHoraria.isEmpty()) {
            return null;
        }
        Date agora = new Date();
        Meteorologia registoMax = null;

        for (Meteorologia registo : previsaoHoraria) {
            if (!registo.getDataHora().before(agora)) {
                if (registoMax == null || registo.getTemperatura() > registoMax.getTemperatura()) {
                    registoMax = registo;
                }
            }
        }
        return registoMax;
    }

    public double calcularPrecipitacaoPorDia(Date diaEscolhido) {
        if (diaEscolhido == null || previsaoHoraria.isEmpty()) {
            return -1;
        }
        double total = 0;
        boolean encontrouDados = false;

        Calendar calEscolhido = Calendar.getInstance();
        calEscolhido.setTime(diaEscolhido);

        Calendar calRegisto = Calendar.getInstance();

        for (Meteorologia registo : previsaoHoraria) {
            calRegisto.setTime(registo.getDataHora());

            boolean mesmoDia = calEscolhido.get(Calendar.YEAR) == calRegisto.get(Calendar.YEAR)
                    && calEscolhido.get(Calendar.DAY_OF_YEAR) == calRegisto.get(Calendar.DAY_OF_YEAR);

            if (mesmoDia) {
                total += registo.getPrecipitacao();
                encontrouDados = true;
            }
        }

        return encontrouDados ? total : -1;
    }
}