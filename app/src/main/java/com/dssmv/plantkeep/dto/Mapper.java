package com.dssmv.plantkeep.dto;

import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.Planta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        p.setPid(dto.getPid());
        return p;
    }

    /** Passa model para DTO (para enviar) */
    public static PlantaDTO planta2PlantaDTO(Planta p) {
        return new PlantaDTO(p.getId(), p.getNome(), p.getEspecie(), p.getPid());
    }

    /** Passa uma lista DTO para uma lista model */
    public static List<Planta> listPlantaDTO2listPlanta(List<PlantaDTO> dtos) {
        List<Planta> plantas = new ArrayList<>();
        for (PlantaDTO dto : dtos) {
            plantas.add(plantaDTO2Planta(dto));
        }
        return plantas;
    }

    /** Passa um MeteorologiaDTO para o model Meteorologia */
    public static Meteorologia meteorologiaDTO2Meteorologia(MeteorologiaDTO dto) throws ParseException {
        SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT);
        Date dataHora = formatador.parse(dto.getDataHora());
        return new Meteorologia(dto.getTemperatura(), dto.getPrecipitacao(), dataHora);
    }

    /** Passa uma lista de MeteorologiaDTO para uma lista do model Meteorologia */
    public static List<Meteorologia> listMeteorologiaDTO2listMeteorologia(List<MeteorologiaDTO> dtos) throws ParseException {
        List<Meteorologia> previsoes = new ArrayList<>();
        for (MeteorologiaDTO dto : dtos) {
            previsoes.add(meteorologiaDTO2Meteorologia(dto));
        }
        return previsoes;
    }
}
