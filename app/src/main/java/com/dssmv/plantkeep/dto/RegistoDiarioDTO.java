package com.dssmv.plantkeep.dto;

/**
 * DTO de um registo do diário da planta.
 * Só campos, construtor sem parâmetros e getters/setters.
 */
public class RegistoDiarioDTO {

    private String texto;
    private long dataHora;
    private String fotoBase64;

    public RegistoDiarioDTO() {
    }

    public RegistoDiarioDTO(String texto, long dataHora, String fotoBase64) {
        this.texto = texto;
        this.dataHora = dataHora;
        this.fotoBase64 = fotoBase64;
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

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }
}