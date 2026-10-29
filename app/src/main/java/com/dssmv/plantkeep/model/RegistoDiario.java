package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model de um registo no diário da planta (estado de crescimento e data/hora).
 */
public class RegistoDiario {

    private String texto;
    private Date dataHora;

    public RegistoDiario(String texto, Date dataHora) {
        setTexto(texto);
        setDataHora(dataHora);
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

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        return sdf.format(dataHora) + "\n" + texto;
    }
}