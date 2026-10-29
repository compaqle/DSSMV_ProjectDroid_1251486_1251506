package com.dssmv.plantkeep.dto;

/**
 * DTO de um registo do diário da planta.
 */
public class RegistoDiarioDTO {

    private String texto;
    private long dataHora;

    public RegistoDiarioDTO() {
    }

    public RegistoDiarioDTO(String texto, long dataHora) {
        this.texto = texto;
        this.dataHora = dataHora;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getDataHora() {
        return dataHora;
    }

    public void setDataHora(long dataHora) {
        this.dataHora = dataHora;
    }
}