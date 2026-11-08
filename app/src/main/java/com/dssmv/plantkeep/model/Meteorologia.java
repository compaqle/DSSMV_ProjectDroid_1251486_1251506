package com.dssmv.plantkeep.model;

import java.util.Date;

public class Meteorologia {

    private double temperatura;
    private double humidadeSolo;
    private Date dataHora;

    public Meteorologia(double temperatura, double humidadeSolo, Date dataHora) {
        setTemperatura(temperatura);
        setHumidadeSolo(humidadeSolo);
        setDataHora(dataHora);
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public double getHumidadeSolo() {
        return humidadeSolo;
    }

    public void setHumidadeSolo(double humidadeSolo) {
        if (humidadeSolo < 0 || humidadeSolo > 100) {
            throw new IllegalArgumentException("Meteorologia: humidade do solo deve estar entre 0% e 100%");
        }
        this.humidadeSolo = humidadeSolo;
    }

    public Date getDataHora() {
        return new Date(dataHora.getTime());
    }

    public void setDataHora(Date dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Meteorologia: dataHora não pode ser nula");
        }
        this.dataHora = new Date(dataHora.getTime());
    }

    @Override
    public String toString() {
        return "Meteorologia{" +
                "temperatura=" + temperatura + " °C, " +
                "humidadeSolo=" + humidadeSolo + "%, " +
                "dataHora=" + dataHora + '}';
    }
}
