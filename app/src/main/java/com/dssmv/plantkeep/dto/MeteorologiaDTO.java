package com.dssmv.plantkeep.dto;

/**
 * DTO da meteorologia. Recipiente de dados igual ao JSON da API Open-Meteo.
 * Só campos, construtor sem parâmetros e getters/setters.
 */
public class MeteorologiaDTO {

    private double temperatura;
    private double precipitacao;
    private double humidadeSolo; // Guardado já em percentagem (0 a 100%)
    private String dataHora;     // Texto ISO tal como vem no JSON (ex: "2026-10-26T14:00")

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