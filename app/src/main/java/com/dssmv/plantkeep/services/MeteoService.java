package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.Mapper;
import com.dssmv.plantkeep.dto.MeteorologiaDTO;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;
import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.MeteorologiaContainer;

import java.util.List;

/**
 * Serviço da meteorologia. Orquestra rede, JSON, DTO, Mapper e atualiza o Container.
 */
public class MeteoService {

    private static final String METEO_URL = "https://api.open-meteo.com/v1/forecast";

    private MeteoService() {} // Só estáticos, nunca instanciar

    /** Vai à API buscar as previsões e guarda-as no MeteorologiaContainer. */
    public static void carregarMeteorologia(double latitude, double longitude) throws Exception {
        List<Meteorologia> previsoes = obterDadosMeteorologicos(latitude, longitude);
        MeteorologiaContainer.getInstance().setPrevisaoHoraria(previsoes);
    }

    /** Devolve a lista de previsões horárias (GET -> JSON -> DTO -> Mapper -> Model). */
    public static List<Meteorologia> obterDadosMeteorologicos(double latitude, double longitude) throws Exception {
        String url = METEO_URL + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&hourly=temperature_2m,precipitation,soil_moisture_1_to_3cm";

        String json = NetworkHandler.getDataInStringFromUrl(url);
        List<MeteorologiaDTO> dtos = JsonHandler.deSerializeJson2ListMeteorologiaDTO(json);
        return Mapper.listMeteorologiaDTO2listMeteorologia(dtos);
    }
}