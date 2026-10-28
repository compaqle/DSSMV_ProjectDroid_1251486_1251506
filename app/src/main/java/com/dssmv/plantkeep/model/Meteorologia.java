package com.dssmv.plantkeep.model;

import java.util.Date;

public class Meteorologia {

    private double temperatura;
    private double precipitacao;
    private double humidadeSolo;
    private Date dataHora;

    public Meteorologia(double temperatura, double precipitacao, double humidadeSolo, Date dataHora) {
        setTemperatura(temperatura);
        setPrecipitacao(precipitacao);
        setHumidadeSolo(humidadeSolo);
        setDataHora(dataHora);
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public double getPrecipitacao() {
        return precipitacao;
    }

    public void setPrecipitacao(double precipitacao) {
        if (precipitacao < 0) {
            throw new IllegalArgumentException("Meteorologia: precipitação não pode ser negativa");
        }
        this.precipitacao = precipitacao;
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
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Meteorologia: dataHora não pode ser nula");
        }
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "Meteorologia{" +
                "temperatura=" + temperatura + "°C, " +
                "precipitacao=" + precipitacao + "mm, " +
                "humidadeSolo=" + humidadeSolo + "%, " +
                "dataHora=" + dataHora + '}';
    }
}