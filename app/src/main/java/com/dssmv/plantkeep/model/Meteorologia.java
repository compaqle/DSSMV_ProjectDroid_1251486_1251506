package com.dssmv.plantkeep.model;

import java.time.LocalDateTime;
public class Meteorologia {

    private double temperatura;
    private double precipitacao;
    private LocalDateTime dataHora;

    public Meteorologia(double temperatura, double precipitacao, LocalDateTime dataHora) {
        this.temperatura = temperatura;
        this.precipitacao = precipitacao;
        this.dataHora = dataHora;
    }

    public double getTemperatura() {return temperatura;}

    public double getPrecipitacao() {return precipitacao;}

    public LocalDateTime getDataHora() {return dataHora;}

    public void setTemperatura(double temperatura) {this.temperatura = temperatura;}

    public void setPrecipitacao(double precipitacao) {this.precipitacao = precipitacao;}

    public void setDataHora(LocalDateTime dataHora) {this.dataHora = dataHora;}

    @Override
    public String toString() {
        return "Meteorologia{" + "temperatura=" + temperatura + "ºC, precipitacao=" + precipitacao + "mm, dataHora=" + dataHora + '}';
    }
}