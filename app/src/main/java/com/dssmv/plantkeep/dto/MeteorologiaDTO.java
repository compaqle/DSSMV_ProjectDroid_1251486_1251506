package com.dssmv.plantkeep.dto;

public class MeteorologiaDTO {

    private final double temperatura;
    private final double humidadeSolo; // Em percentagem
    private final String dataHora;     // Data em formato ISO (ex: "2026-10-26T14:00")

    public MeteorologiaDTO(double temperatura, double humidadeSolo, String dataHora) {
        this.temperatura = temperatura;
        this.humidadeSolo = humidadeSolo;
        this.dataHora = dataHora;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public double getHumidadeSolo() {
        return humidadeSolo;
    }

    public String getDataHora() {
        return dataHora;
    }

}
