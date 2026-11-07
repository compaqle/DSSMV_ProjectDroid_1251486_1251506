package com.dssmv.plantkeep.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MeteorologiaContainer {

    private interface ComparadorRegisto {
        boolean escolherNovo(Meteorologia candidato, Meteorologia atual);
    }

    private volatile List<Meteorologia> previsaoHoraria;
    private static MeteorologiaContainer instancia;

    private MeteorologiaContainer() {
        this.previsaoHoraria = new ArrayList<>();
    }

    public static synchronized MeteorologiaContainer getInstance() {
        if (instancia == null) {
            instancia = new MeteorologiaContainer();
        }
        return instancia;
    }

    public synchronized List<Meteorologia> getPrevisaoHoraria() {
        return Collections.unmodifiableList(previsaoHoraria);
    }

    public synchronized void setPrevisaoHoraria(List<Meteorologia> previsaoHoraria) {
        this.previsaoHoraria = (previsaoHoraria != null) ? new ArrayList<>(previsaoHoraria) : new ArrayList<>();
    }

    // Devolve o registo mais frio previsto a partir de agora
    public synchronized Meteorologia obterRegistoTemperaturaMinimaPrevista() {
        return procurarExtremo((r, min) -> r.getTemperatura() < min.getTemperatura());
    }

    // Devolve o registo mais quente previsto a partir de agora
    public synchronized Meteorologia obterRegistoTemperaturaMaximaPrevista() {
        return procurarExtremo((r, max) -> r.getTemperatura() > max.getTemperatura());
    }

    // Devolve o registo com maior humidade do solo prevista a partir de agora
    public synchronized Meteorologia obterRegistoHumidadeSoloMaximaPrevista() {
        return procurarExtremo((r, max) -> r.getHumidadeSolo() > max.getHumidadeSolo());
    }

    // Procura nas previsões futuras o registo que cumpre o critério de comparação
    private Meteorologia procurarExtremo(ComparadorRegisto comparador) {
        Date agora = new Date();
        Meteorologia escolhido = null;
        for (int i = 0; i < previsaoHoraria.size(); i++) {
            Meteorologia registo = previsaoHoraria.get(i);
            if (registo != null && registo.getDataHora() != null && !registo.getDataHora().before(agora)) {
                if (escolhido == null || comparador.escolherNovo(registo, escolhido)) {
                    escolhido = registo;
                }
            }
        }
        return escolhido;
    }

    // Devolve o primeiro registo em que a humidade do solo fica abaixo do mínimo
    public synchronized Meteorologia obterPrimeiroRegistoAbaixoHumidadeMinima(int minSoilMoist, Date aPartirDe) {
        Date inicio = (aPartirDe != null) ? new Date(aPartirDe.getTime()) : new Date();
        for (int i = 0; i < previsaoHoraria.size(); i++) {
            Meteorologia registo = previsaoHoraria.get(i);
            if (registo != null && registo.getDataHora() != null && !registo.getDataHora().before(inicio)) {
                if (registo.getHumidadeSolo() < minSoilMoist) {
                    return registo;
                }
            }
        }
        return null;
    }
}