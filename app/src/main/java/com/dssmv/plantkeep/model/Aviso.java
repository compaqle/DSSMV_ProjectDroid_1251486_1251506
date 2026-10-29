package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private final Planta planta;
    private final String nomePlanta;
    private final TipoAviso tipo;
    private final String mensagem;
    private final Date dataHora;

    public Aviso(Planta planta, TipoAviso tipo, String mensagem, Date dataHora) {
        if (planta == null || planta.getNome() == null || planta.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da planta inválido");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo é obrigatório");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("dataHora não pode ser nula");
        }
        this.planta = planta;
        this.nomePlanta = planta.getNome().trim();
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
    }

    public Planta getPlanta() {
        return planta;
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
        if (diffMs <= 0) {
            return "hoje";
        }
        // Calcula os dias arredondando para cima (ex: 72 horas = 3 dias)
        long dias = (long) Math.ceil(diffMs / (1000.0 * 60 * 60 * 24));

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
        return nomePlanta + " (" + sdf.format(dataHora) + "): " + mensagem;
    }
}