package com.dssmv.plantkeep.dto;

import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;

/**
 * O Mapper. Único sito que converte entre o DTO e o model.
 * Não faz HTTP nem JSON.
 */
public class Mapper {

    private Mapper() {}   // só métodos estáticos, nunca instaciar

    /** Passa DTO para model (valida o nomme) */
    public static Planta plantaDTO2Planta(PlantaDTO dto) {
        Planta p = new Planta(dto.getNome(), dto.getEspecie());
        p.setId(dto.getId());
        return p;
    }

    /** Passa model para DTO (para enviar) */
    public static PlantaDTO planta2PlantaDTO(Planta p) {
        return new PlantaDTO(p.getId(), p.getNome(), p.getEspecie());
    }

    /** Passa uma lista DTO para uma lista model */
    public static List<Planta> listPlantaDTO2listPlanta(List<PlantaDTO> dtos) {
        List<Planta> plantas = new ArrayList<>();
        for (PlantaDTO dto : dtos) {
            plantas.add(plantaDTO2Planta(dto));
        }
        return plantas;
    }
}
