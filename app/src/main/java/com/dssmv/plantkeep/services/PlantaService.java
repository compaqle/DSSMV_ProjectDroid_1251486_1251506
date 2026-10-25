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
        String json = new NetworkHandler("planta").list();
        List<PlantaDTO> dtos = JsonHandler.deSerializeJson2ListPlantaDTO(json);
        return Mapper.listPlantaDTO2listPlanta(dtos);
    }

    /** Cria uma planta (POST). */
    public static void criar(Planta p) throws Exception {
        PlantaDTO dto = Mapper.planta2PlantaDTO(p);
        String json = JsonHandler.serializePlantaDTO2Json(dto);
        new NetworkHandler("planta").create(json);
    }

    /** Apaga uma planta (DELETE). */
    public static void apagar(Planta p) throws Exception {
        if (p.getId() != null) {
            new NetworkHandler("planta").delete(p.getId());
        }
    }
}
