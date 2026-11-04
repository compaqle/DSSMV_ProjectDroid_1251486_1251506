package com.dssmv.plantkeep.dto;

/**
 * DTO da meteorologia. Recebe os dados da API Open-Meteo.
 */
public class MeteorologiaDTO {

    private double temperatura;
    private double precipitacao;
    private double humidadeSolo; // Em percentagem
    private String dataHora;     // Data em formato ISO (ex: "2026-10-26T14:00")

    public MeteorologiaDTO() {
    }

    public MeteorologiaDTO(double temperatura, double precipitacao, double humidadeSolo, String dataHora) {
        this.temperatura = temperatura;
        this.precipitacao = precipitacao;
        this.humidadeSolo = humidadeSolo;
        this.dataHora = dataHora;
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
        this.precipitacao = precipitacao;
    }

    public double getHumidadeSolo() {
        return humidadeSolo;
    }

    public void setHumidadeSolo(double humidadeSolo) {
        this.humidadeSolo = humidadeSolo;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }
}