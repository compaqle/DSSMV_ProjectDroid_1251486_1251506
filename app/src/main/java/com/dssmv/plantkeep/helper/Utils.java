package com.dssmv.plantkeep.helper;

/**
 * Constantes centrais de configuração dos serviços (URLs dos servidores).
 * Concentra aqui o que está hardcoded, para não ficar espalhado pelos services.
 */
public final class Utils {

    private Utils() {}   // só constantes, nunca instanciar

    /** Base da API OpenPlantbook (https://open.plantbook.io). */
    public static final String OPB_BASE = "https://open.plantbook.io/api/v1";

    /** Endpoint de previsões horárias do Open-Meteo (https://open-meteo.com). */
    public static final String METEO_URL = "https://api.open-meteo.com/v1/forecast";
}
