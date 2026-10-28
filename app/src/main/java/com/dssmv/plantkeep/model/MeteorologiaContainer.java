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

    /** Devolve o registo mais frio previsto daqui para a frente. */
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

    /** Devolve o registo mais quente previsto daqui para a frente. */
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

    public Meteorologia obterRegistoHumidadeSoloMinimaPrevista() {
        if (previsaoHoraria.isEmpty()) {
            return null;
        }
        Date agora = new Date();
        Meteorologia registoMin = null;

        for (Meteorologia registo : previsaoHoraria) {
            if (!registo.getDataHora().before(agora)) {
                if (registoMin == null || registo.getHumidadeSolo() < registoMin.getHumidadeSolo()) {
                    registoMin = registo;
                }
            }
        }
        return registoMin;
    }

    public Meteorologia obterRegistoHumidadeSoloMaximaPrevista() {
        if (previsaoHoraria.isEmpty()) {
            return null;
        }
        Date agora = new Date();
        Meteorologia registoMax = null;

        for (Meteorologia registo : previsaoHoraria) {
            if (!registo.getDataHora().before(agora)) {
                if (registoMax == null || registo.getHumidadeSolo() > registoMax.getHumidadeSolo()) {
                    registoMax = registo;
                }
            }
        }
        return registoMax;
    }
}