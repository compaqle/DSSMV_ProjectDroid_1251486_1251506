package com.dssmv.plantkeep.dto;

/**
 * DTO da meteorologia. Recipiente de dados igual ao JSON da API Open-Meteo.
 */
public class MeteorologiaDTO {

    private double temperatura;
    private double precipitacao;
    private String dataHora; // Texto ISO tal como vem no JSON (ex: "2026-10-26T14:00")

    public MeteorologiaDTO() {
    }

    public MeteorologiaDTO(double temperatura, double precipitacao, String dataHora) {
        this.temperatura = temperatura;
        this.precipitacao = precipitacao;
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

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }
}