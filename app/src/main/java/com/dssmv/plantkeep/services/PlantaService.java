package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.Mapper;
import com.dssmv.plantkeep.dto.PlantaDTO;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.RegistoDiario;

import java.util.Date;
import java.util.List;

/**
 * Serviço das plantas. Orquestra rede, JSON, DTO e Mapper.
 */
public class PlantaService {

    private PlantaService() {}   // só estáticos, nunca instanciar

    /** Devolve a lista de plantas (GET). */
    public static List<Planta> getPlantas() throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = NetworkHandler.getDataInStringFromUrl(url);
        List<PlantaDTO> dtos = JsonHandler.deSerializeJson2ListPlantaDTO(json);
        return Mapper.listPlantaDTO2listPlanta(dtos);
    }

    /** Procura e devolve uma planta específica pelo seu ID. */
    public static Planta obterPlantaPorId(String id) throws Exception {
        if (id == null || id.isEmpty()) {
            return null;
        }
        List<Planta> plantas = getPlantas();
        for (Planta p : plantas) {
            if (id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    /** Cria uma planta (POST). */
    public static void criar(Planta p) throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(p));
        NetworkHandler.addDataInStringFromUrl(url, json);
    }

    /** Marca uma planta como regada agora e atualiza no RestDB (PUT). */
    public static void regar(Planta p) throws Exception {
        if (p != null && p.getId() != null) {
            Planta atual = obterPlantaPorId(p.getId());
            if (atual == null) {
                atual = p;
            }
            atual.setUltimaRega(System.currentTimeMillis());
            String url = NetworkHandler.BASE + "/planta/" + atual.getId();
            String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(atual));
            NetworkHandler.updateDataInStringFromUrl(url, json);
        }
    }

    /** Adiciona um novo registo de texto ao diário da planta e atualiza no RestDB (PUT). */
    public static Planta adicionarRegistoDiario(String plantaId, String texto) throws Exception {
        Planta p = obterPlantaPorId(plantaId);
        if (p == null) {
            throw new IllegalArgumentException("Planta não encontrada.");
        }
        p.adicionarRegistoDiario(new RegistoDiario(texto, new Date()));

        String url = NetworkHandler.BASE + "/planta/" + p.getId();
        String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(p));
        NetworkHandler.updateDataInStringFromUrl(url, json);
        return p;
    }

    /** Apaga uma planta (DELETE). */
    public static void apagar(Planta p) throws Exception {
        if (p.getId() != null) {
            String url = NetworkHandler.BASE + "/planta/" + p.getId();
            NetworkHandler.deleteDataInStringFromUrl(url);
        }
    }
}