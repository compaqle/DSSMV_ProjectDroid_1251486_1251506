package com.dssmv.plantkeep.model;

import com.dssmv.plantkeep.dto.Mapper;
import com.dssmv.plantkeep.dto.PlantaDTO;
import com.dssmv.plantkeep.handler.JsonHandler;
import com.dssmv.plantkeep.handler.NetworkHandler;

import java.util.List;

/**
 * O orquestrador. Monta cada operação na ordem: rede, JSON, DTO, Mapper, model.
 */
public class PlantaContainer {
    /** Da o resultado ao Controller (em thread de fundo) */
    public interface Callback {
        void onSucesso(List<Planta> plantas);
        void onErro(String msg);
    }

    private final NetworkHandler network;

    public PlantaContainer() {
        this.network = new NetworkHandler("planta");
    }

    /** Cria a planta (POST) e, ao fim, devolve a lista atualizada. */
    public void criar(final Planta p, final Callback cb) {
        new Thread(() -> {
            try {
                PlantaDTO dto = Mapper.planta2PlantaDTO(p);                      // model -> DTO
                network.create(JsonHandler.serializePlantaDTO2Json(dto));         // DTO -> JSON -> HTTP
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Devolve a lista de plantas (GET). */
    public void listar(final Callback cb) {
        new Thread(() -> {
            try {
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Apaga uma planta (DELETE) e deovlve a lista. */
    public void apagar(final Planta p, final Callback cb) {
        new Thread(() -> {
            try {
                if (p.getId() != null) {
                    network.delete(p.getId());
                }
                cb.onSucesso(getAll());
            } catch (Exception e) {
                cb.onErro(e.getMessage());
            }
        }).start();
    }

    /** Leitura interna (GET) usada pelos metodos acima */
    private List<Planta> getAll() throws Exception {
        String json = network.list();                                             // HTTP -> texto
        List<PlantaDTO> dtos = JsonHandler.deSerializeJson2ListPlantaDTO(json);   // texto -> DTO
        return Mapper.listPlantaDTO2listPlanta(dtos);                             // DTO -> model
    }
}
