package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.Mapper;
import com.dssmv.plantkeep.dto.PlantaDTO;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;
import com.dssmv.plantkeep.model.Planta;

import java.util.List;

/**
 * Serviço das plantas. Orquestra rede, JSON, DTO e Mapper.
 */
public class PlantaService {

    private PlantaService() {}   // só estáticos, nunca instaciar

    /** Devolve a lista de plantas (GET). */
    public static List<Planta> getPlantas() throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = NetworkHandler.getDataInStringFromUrl(url);
        List<PlantaDTO> dtos = JsonHandler.deSerializeJson2ListPlantaDTO(json);
        return Mapper.listPlantaDTO2listPlanta(dtos);
    }

    /** Cria uma planta (POST). */
    public static void criar(Planta p) throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(p));
        NetworkHandler.addDataInStringFromUrl(url, json);
    }

    /** Apaga uma planta (DELETE). */
    public static void apagar(Planta p) throws Exception {
        if (p.getId() != null) {
            String url = NetworkHandler.BASE + "/planta/" + p.getId();
            NetworkHandler.deleteDataInStringFromUrl(url);
        }
    }
}
