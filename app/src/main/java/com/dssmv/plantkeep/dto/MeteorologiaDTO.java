package com.dssmv.plantkeep.dto;

/**
 * DTO da meteorologia. Recebe os dados da API Open-Meteo.
 */
public class MeteorologiaDTO {

    private final double temperatura;
    private final double precipitacao;
    private final double humidadeSolo; // Em percentagem
    private final String dataHora;     // Data em formato ISO (ex: "2026-10-26T14:00")

    public MeteorologiaDTO(double temperatura, double precipitacao, double humidadeSolo, String dataHora) {
        this.temperatura = temperatura;
        this.precipitacao = precipitacao;
        this.humidadeSolo = humidadeSolo;
        this.dataHora = dataHora;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getPrecipitacao() {
        return precipitacao;
    }


    public double getHumidadeSolo() {
        return humidadeSolo;
    }

    public String getDataHora() {
        return dataHora;
    }

}