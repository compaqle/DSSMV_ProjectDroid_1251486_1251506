package com.dssmv.plantkeep.model;

import java.util.Date;

public class Meteorologia {

    private double temperatura;
    private double precipitacao;
    private Date dataHora;

    public Meteorologia(double temperatura, double precipitacao, Date dataHora) {
        this.temperatura = temperatura;
        this.precipitacao = precipitacao;
        this.dataHora = dataHora;
    }

    public double getTemperatura() {return temperatura;}

    public double getPrecipitacao() {return precipitacao;}

    public Date getDataHora() {return dataHora;}

    public void setTemperatura(double temperatura) {this.temperatura = temperatura;}

    public void setPrecipitacao(double precipitacao) {this.precipitacao = precipitacao;}

    public void setDataHora(Date dataHora) {this.dataHora = dataHora;}

    @Override
    public String toString() {
        return "Meteorologia{" + "temperatura=" + temperatura + "ºC, precipitacao=" + precipitacao + "mm, dataHora=" + dataHora + '}';
    }
}