package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model de um Aviso/Alerta para uma planta, incluindo a data e hora do evento.
 * Preparado com TipoAviso para no futuro suportar chuva, vento, humidade, etc.
 */
public class Aviso {

    public enum TipoAviso {
        TEMPERATURA_BAIXA,
        TEMPERATURA_ALTA
    }

    private final String nomePlanta;
    private final TipoAviso tipo;
    private final String mensagem;
    private final Date dataHora;

    public Aviso(String nomePlanta, TipoAviso tipo, String mensagem, Date dataHora) {
        if (nomePlanta == null || nomePlanta.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da planta inválido");
        }
        if (tipo == null || mensagem == null || mensagem.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo e mensagem são obrigatórios");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("DataHora não pode ser nula");
        }
        this.nomePlanta = nomePlanta.trim();
        this.tipo = tipo;
        this.mensagem = mensagem.trim();
        this.dataHora = dataHora;
    }

    public String getNomePlanta() {
        return nomePlanta;
    }

    public TipoAviso getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Date getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
        return nomePlanta + " (" + sdf.format(dataHora) + "): " + mensagem;
    }
}