package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Model de um Aviso/Alerta para uma planta, incluindo a data e hora do evento.
 */
public class Aviso {

    public enum TipoAviso {
        TEMPERATURA_BAIXA,
        TEMPERATURA_ALTA,
        HUMIDADE_SOLO_BAIXA,
        HUMIDADE_SOLO_ALTA
    }

    private final String nomePlanta;
    private final TipoAviso tipo;
    private final String mensagem;
    private final Date dataHora;

    public Aviso(String nomePlanta, TipoAviso tipo, String mensagem, Date dataHora) {
        if (nomePlanta == null || nomePlanta.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da planta inválido");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo é obrigatório");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("dataHora não pode ser nula");
        }
        this.nomePlanta = nomePlanta.trim();
        this.tipo = tipo;
        this.mensagem = mensagem;
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

    private String formatarData() {
        long diffMs = dataHora.getTime() - System.currentTimeMillis();
        long dias = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (dias <= 0) {
            return "hoje";
        } else if (dias == 1) {
            return "em 1 dia";
        } else {
            return "em " + dias + " dias";
        }
    }

    @Override
    public String toString() {
        if (tipo == TipoAviso.HUMIDADE_SOLO_BAIXA) {
            return nomePlanta + " " + formatarData();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
        return  nomePlanta + " (" + sdf.format(dataHora) + "): " + mensagem;
    }
}