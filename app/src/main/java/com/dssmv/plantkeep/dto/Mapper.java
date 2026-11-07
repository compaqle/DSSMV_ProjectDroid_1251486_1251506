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

/**
 * O Mapper. Único sítio que converte entre o DTO e o model.
 * Não faz HTTP nem JSON.
 */
public class Mapper {

    private Mapper() {}   // só métodos estáticos, nunca instanciar

    /** Passa RegistoDiarioDTO para o model RegistoDiario */
    public static RegistoDiario registoDiarioDTO2RegistoDiario(RegistoDiarioDTO dto) {
        return new RegistoDiario(dto.getTexto(), new Date(dto.getDataHora()), dto.getFotoBase64());
    }

    /** Passa model RegistoDiario para RegistoDiarioDTO */
    public static RegistoDiarioDTO registoDiario2RegistoDiarioDTO(RegistoDiario registo) {
        return new RegistoDiarioDTO(registo.getTexto(), registo.getDataHora().getTime(), registo.getFotoBase64());
    }

    /** Passa DTO para model (valida o nome) */
    public static Planta plantaDTO2Planta(PlantaDTO dto) {
        Planta p = new Planta(dto.getNome(), dto.getEspecie());
        p.setId(dto.getId());
        p.setPid(dto.getPid());
        p.setUltimaRega(dto.getUltimaRega());

        List<RegistoDiario> registos = new ArrayList<>();
        if (dto.getDiario() != null) {
            for (RegistoDiarioDTO rDto : dto.getDiario()) {
                registos.add(registoDiarioDTO2RegistoDiario(rDto));
            }
        }
        p.setDiario(registos);
        return p;
    }

    /** Passa model para DTO (para enviar) */
    public static PlantaDTO planta2PlantaDTO(Planta p) {
        List<RegistoDiarioDTO> registosDto = new ArrayList<>();
        if (p.getDiario() != null) {
            for (RegistoDiario r : p.getDiario()) {
                registosDto.add(registoDiario2RegistoDiarioDTO(r));
            }
        }
        return new PlantaDTO(p.getId(), p.getNome(), p.getEspecie(), p.getPid(), p.getUltimaRega(), registosDto);
    }

    /** Passa uma lista DTO para uma lista model */
    public static List<Planta> listPlantaDTO2listPlanta(List<PlantaDTO> dtos) {
        List<Planta> plantas = new ArrayList<>();
        if (dtos == null) {
            return plantas;
        }
        for (PlantaDTO dto : dtos) {
            if (dto != null) {
                plantas.add(plantaDTO2Planta(dto));
            }
        }
        return plantas;
    }

    /** Passa um MeteorologiaDTO para o model Meteorologia */
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

    /** Passa uma lista de MeteorologiaDTO para uma lista do model Meteorologia */
    public static List<Meteorologia> listMeteorologiaDTO2listMeteorologia(List<MeteorologiaDTO> dtos) throws ParseException {
        List<Meteorologia> previsoes = new ArrayList<>();
        if (dtos == null) {
            return previsoes;
        }
        for (MeteorologiaDTO dto : dtos) {
            if (dto != null) {
                previsoes.add(meteorologiaDTO2Meteorologia(dto));
            }
        }
        return previsoes;
    }

    /** Converte um EspecieOpb (DTO) para o model Especie  */
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
                textoSeguro(dto.getAlias()),
                textoSeguro(dto.getCategory()),
                textoSeguro(dto.getImageUrl())
        );
    }

    /** Converte uma lista de EspecieOpb (DTOs) para uma lista de models Especie  */
    public static List<Especie> paraListaEspecies(List<EspecieOpb> dtos) {
        List<Especie> especies = new ArrayList<>();
        if (dtos == null) {
            return especies;
        }
        for (EspecieOpb dto : dtos) {
            Especie modelo = paraModelo(dto);
            if (modelo != null) {
                especies.add(modelo);
            }
        }
        return especies;
    }

    /** Converte um DetalheOpb (DTO) para o model CuidadosEspecie  */
    public static CuidadosEspecie paraModelo(DetalheOpb dto) {
        if (dto == null) {
            return null;
        }
        Integer minTemp = menorLimite(dto.getMinTemp(), dto.getMaxTemp());
        Integer maxTemp = maiorLimite(dto.getMinTemp(), dto.getMaxTemp());
        Integer minEnvHumid = menorLimite(dto.getMinEnvHumid(), dto.getMaxEnvHumid());
        Integer maxEnvHumid = maiorLimite(dto.getMinEnvHumid(), dto.getMaxEnvHumid());
        Integer minLightLux = menorLimite(dto.getMinLightLux(), dto.getMaxLightLux());
        Integer maxLightLux = maiorLimite(dto.getMinLightLux(), dto.getMaxLightLux());
        Integer minSoilMoist = menorLimite(dto.getMinSoilMoist(), dto.getMaxSoilMoist());
        Integer maxSoilMoist = maiorLimite(dto.getMinSoilMoist(), dto.getMaxSoilMoist());
        Integer minSoilEc = menorLimite(dto.getMinSoilEc(), dto.getMaxSoilEc());
        Integer maxSoilEc = maiorLimite(dto.getMinSoilEc(), dto.getMaxSoilEc());

        return new CuidadosEspecie(
                minTemp,
                maxTemp,
                minEnvHumid,
                maxEnvHumid,
                minLightLux,
                maxLightLux,
                minSoilMoist,
                maxSoilMoist,
                minSoilEc,
                maxSoilEc,
                textoSeguro(dto.getCategory()),
                textoSeguro(dto.getOrigin()),
                textoSeguro(dto.getImageUrl())
        );
    }

    private static String textoSeguro(String valor) {
        return (valor != null) ? valor.trim() : "";
    }

    private static Integer menorLimite(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            return max;
        }
        return min;
    }

    private static Integer maiorLimite(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            return min;
        }
        return max;
    }
}