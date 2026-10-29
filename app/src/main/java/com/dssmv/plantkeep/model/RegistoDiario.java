package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model de um registo no diário da planta (texto, data/hora e fotografia opcional em Base64).
 */
public class RegistoDiario {

    private String texto;
    private Date dataHora;
    private String fotoBase64; // Fotografia codificada em Base64

    public RegistoDiario(String texto, Date dataHora) {
        this(texto, dataHora, "");
    }

    public RegistoDiario(String texto, Date dataHora, String fotoBase64) {
        setTexto(texto);
        setDataHora(dataHora);
        setFotoBase64(fotoBase64);
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Diário: o texto do registo não pode ser vazio");
        }
        this.texto = texto.trim();
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        if (dataHora == null) {
            throw new IllegalArgumentException("Diário: a data não pode ser nula");
        }
        this.dataHora = dataHora;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = (fotoBase64 != null) ? fotoBase64 : "";
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        return sdf.format(dataHora) + "\n" + texto;
    }
}