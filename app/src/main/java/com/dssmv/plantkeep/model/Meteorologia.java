package com.dssmv.plantkeep.model;

import java.util.Date;

public class Meteorologia {

    private double temperatura;
    private double precipitacao;
    private Date dataHora;

    public Meteorologia(double temperatura, double precipitacao, Date dataHora) {
        setTemperatura(temperatura);
        setPrecipitacao(precipitacao);
        setDataHora(dataHora);
    }

    public double getTemperatura() {return temperatura;}

    public double getPrecipitacao() {return precipitacao;}

    public Date getDataHora() {return dataHora;}

    public void setTemperatura(double temperatura) {this.temperatura = temperatura;}

    public void setPrecipitacao(double precipitacao) {
        if (precipitacao < 0) {
            throw new IllegalArgumentException("Meteorologia: precipitação não pode ser negativa");
        }
        this.precipitacao = precipitacao;
    }
    public void setDataHora(Date dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Meteorologia: dataHora não pode ser nula");
        }
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "Meteorologia{" + "temperatura=" + temperatura + "ºC, precipitacao=" + precipitacao + "mm, dataHora=" + dataHora + '}';
    }
}