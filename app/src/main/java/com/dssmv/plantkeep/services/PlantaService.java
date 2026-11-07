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
 * Serviço da planta
 * Gere as operações das plantas no RestDB
 */
public class PlantaService {

    private PlantaService() {}

    // Obtém a lista de todas as plantas
    public static List<Planta> getPlantas() throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = NetworkHandler.getDataInStringFromUrl(url);
        List<PlantaDTO> dtos = JsonHandler.deSerializeJson2ListPlantaDTO(json);
        return Mapper.listPlantaDTO2listPlanta(dtos);
    }

    // Procura uma planta pelo seu ID
    public static Planta obterPlantaPorId(String id) throws Exception {
        if (id == null || id.isEmpty()) {
            return null;
        }
        List<Planta> plantas = getPlantas();
        for (int i = 0; i < plantas.size(); i++) {
            Planta p = plantas.get(i);
            if (p != null && id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    // Guarda uma nova planta na base de dados
    public static void criar(Planta p) throws Exception {
        String url = NetworkHandler.BASE + "/planta";
        String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(p));
        NetworkHandler.addDataInStringFromUrl(url, json);
    }

    // Atualiza a data da última rega da planta diretamente no RestDB
    public static void regar(Planta p) throws Exception {
        if (p != null && p.getId() != null) {
            p.setUltimaRega(System.currentTimeMillis());
            atualizarPlantaNoRestDb(p);
        }
    }

    // Adiciona um registo ao diário da planta e atualiza no RestDB
    public static Planta adicionarRegistoDiario(String plantaId, String texto, String fotoBase64) throws Exception {
        Planta p = obterPlantaPorId(plantaId);
        if (p == null) {
            throw new IllegalArgumentException("Planta não encontrada.");
        }
        p.adicionarRegistoDiario(new RegistoDiario(texto, new Date(), fotoBase64));
        atualizarPlantaNoRestDb(p);
        return p;
    }

    // Remove um registo do diário e atualiza a planta no RestDB
    public static Planta removerRegistoDiario(String plantaId, RegistoDiario registo) throws Exception {
        Planta p = obterPlantaPorId(plantaId);
        if (p == null) {
            throw new IllegalArgumentException("Planta não encontrada.");
        }
        p.removerRegistoDiario(registo);
        atualizarPlantaNoRestDb(p);
        return p;
    }

    private static void atualizarPlantaNoRestDb(Planta p) throws Exception {
        String url = NetworkHandler.BASE + "/planta/" + p.getId();
        String json = JsonHandler.serializePlantaDTO2Json(Mapper.planta2PlantaDTO(p));
        NetworkHandler.updateDataInStringFromUrl(url, json);
    }

    // Apaga uma planta do RestDB
    public static void apagar(Planta p) throws Exception {
        if (p != null && p.getId() != null) {
            String url = NetworkHandler.BASE + "/planta/" + p.getId();
            NetworkHandler.deleteDataInStringFromUrl(url);
        }
    }
}