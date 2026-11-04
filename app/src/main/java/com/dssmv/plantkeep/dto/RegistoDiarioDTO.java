package com.dssmv.plantkeep.dto;

/**
 * DTO de um registo do diário da planta.
 */
public class RegistoDiarioDTO {

    private final String texto;
    private final long dataHora;
    private final String fotoBase64;

    public RegistoDiarioDTO(String texto, long dataHora, String fotoBase64) {
        this.texto = texto;
        this.dataHora = dataHora;
        this.fotoBase64 = fotoBase64;
    }

    public String getTexto() {
        return texto;
    }

    public long getDataHora() {
        return dataHora;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }
}