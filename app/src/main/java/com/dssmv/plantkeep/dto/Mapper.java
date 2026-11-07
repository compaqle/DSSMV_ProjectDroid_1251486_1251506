package com.dssmv.plantkeep.dto;

import com.dssmv.plantkeep.model.CuidadosEspecie;
import com.dssmv.plantkeep.model.Especie;
import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.RegistoDiario;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Mapper {

    private Mapper() {} // Só métodos estáticos, nunca instanciar

    // Passa RegistoDiarioDTO para o model RegistoDiario
    public static RegistoDiario registoDiarioDTO2RegistoDiario(RegistoDiarioDTO dto) {
        return new RegistoDiario(dto.getTexto(), new Date(dto.getDataHora()), dto.getFotoBase64());
    }

    // Passa model RegistoDiario para RegistoDiarioDTO
    public static RegistoDiarioDTO registoDiario2RegistoDiarioDTO(RegistoDiario registo) {
        return new RegistoDiarioDTO(registo.getTexto(), registo.getDataHora().getTime(), registo.getFotoBase64());
    }

    // Passa PlantaDTO para o model Planta
    public static Planta plantaDTO2Planta(PlantaDTO dto) {
        Planta p = new Planta(dto.getNome(), dto.getEspecie());
        p.setId(dto.getId());
        p.setPid(dto.getPid());
        p.setUltimaRega(dto.getUltimaRega());
        List<RegistoDiario> registos = new ArrayList<>();
        if (dto.getDiario() != null) {
            List<RegistoDiarioDTO> diario = dto.getDiario();
            for (int i = 0; i < diario.size(); i++) {
                RegistoDiarioDTO rDto = diario.get(i);
                if (rDto != null) {
                    registos.add(registoDiarioDTO2RegistoDiario(rDto));
                }
            }
        }
        p.setDiario(registos);
        return p;
    }

    // Passa model Planta para PlantaDTO
    public static PlantaDTO planta2PlantaDTO(Planta p) {
        List<RegistoDiarioDTO> registosDto = new ArrayList<>();
        if (p.getDiario() != null) {
            List<RegistoDiario> diario = p.getDiario();
            for (int i = 0; i < diario.size(); i++) {
                RegistoDiario r = diario.get(i);
                if (r != null) {
                    registosDto.add(registoDiario2RegistoDiarioDTO(r));
                }
            }
        }
        return new PlantaDTO(p.getId(), p.getNome(), p.getEspecie(), p.getPid(), p.getUltimaRega(), registosDto);
    }

    // Passa uma lista de PlantaDTO para uma lista de Planta
    public static List<Planta> listPlantaDTO2listPlanta(List<PlantaDTO> dtos) {
        List<Planta> plantas = new ArrayList<>();
        if (dtos == null) {
            return plantas;
        }
        for (int i = 0; i < dtos.size(); i++) {
            PlantaDTO dto = dtos.get(i);
            if (dto != null) {
                plantas.add(plantaDTO2Planta(dto));
            }
        }
        return plantas;
    }

    // Passa um MeteorologiaDTO para o model Meteorologia
    public static Meteorologia meteorologiaDTO2Meteorologia(MeteorologiaDTO dto) throws ParseException {
        SimpleDateFormat formatador = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT);
        Date dataHora = formatador.parse(dto.getDataHora());
        return new Meteorologia(
                dto.getTemperatura(),
                dto.getPrecipitacao(),
                dto.getHumidadeSolo(),
                dataHora
        );
    }

    // Passa uma lista de MeteorologiaDTO para uma lista do model Meteorologia
    public static List<Meteorologia> listMeteorologiaDTO2listMeteorologia(List<MeteorologiaDTO> dtos) throws ParseException {
        List<Meteorologia> previsoes = new ArrayList<>();
        if (dtos == null) {
            return previsoes;
        }
        for (int i = 0; i < dtos.size(); i++) {
            MeteorologiaDTO dto = dtos.get(i);
            if (dto != null) {
                previsoes.add(meteorologiaDTO2Meteorologia(dto));
            }
        }
        return previsoes;
    }

    // Converte um EspecieOpb para o model Especie
    public static Especie paraModelo(EspecieOpb dto) {
        if (dto == null) {
            return null;
        }
        String pid = textoSeguro(dto.getPid());
        String displayPid = textoSeguro(dto.getDisplayPid());
        if (pid.isEmpty() && !displayPid.isEmpty()) {
            pid = displayPid;
        }
        if (pid.isEmpty()) {
            return null;
        }
        String nomeCientifico = displayPid.isEmpty() ? pid : displayPid;
        return new Especie(
                pid,
                nomeCientifico,
                textoSeguro(dto.getAlias())
        );
    }

    // Converte uma lista de EspecieOpb para uma lista de Especie
    public static List<Especie> paraListaEspecies(List<EspecieOpb> dtos) {
        List<Especie> especies = new ArrayList<>();
        if (dtos == null) {
            return especies;
        }
        for (int i = 0; i < dtos.size(); i++) {
            EspecieOpb dto = dtos.get(i);
            Especie modelo = paraModelo(dto);
            if (modelo != null) {
                especies.add(modelo);
            }
        }
        return especies;
    }

    // Converte um DetalheOpb para o model CuidadosEspecie
    public static CuidadosEspecie paraModelo(DetalheOpb dto) {
        if (dto == null) {
            return null;
        }
        return new CuidadosEspecie(
                dto.getMinTemp(),
                dto.getMaxTemp(),
                dto.getMinEnvHumid(),
                dto.getMaxEnvHumid(),
                dto.getMinLightLux(),
                dto.getMaxLightLux(),
                dto.getMinSoilMoist(),
                dto.getMaxSoilMoist(),
                dto.getMinSoilEc(),
                dto.getMaxSoilEc(),
                textoSeguro(dto.getCategory()),
                textoSeguro(dto.getOrigin()),
                textoSeguro(dto.getImageUrl())
        );
    }

    private static String textoSeguro(String valor) {
        return (valor != null) ? valor.trim() : "";
    }
}