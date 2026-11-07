package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.CuidadosEspecie;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de Avisos
 * Comparar as plantas do utilizador com a meteorologia e obter a lista de avisos.
 */
public class AvisoService {

    private AvisoService() {}

    // Obtém todos os avisos meteorológicos ativos para as plantas do utilizador.
    public static List<Aviso> obterAvisosAtuais() throws Exception {
        return gerarAvisos(MeteorologiaContainer.getInstance());
    }

    // Gera a lista de avisos meteorológicos para todas as plantas
    public static List<Aviso> gerarAvisos(MeteorologiaContainer meteo) throws Exception {
        List<Aviso> avisos = new ArrayList<>();
        if (meteo == null || meteo.getPrevisaoHoraria().isEmpty()) {
            return avisos;
        }
        List<Planta> plantas = PlantaService.getPlantas();
        for (int i = 0; i < plantas.size(); i++) {
            Planta p = plantas.get(i);
            if (p == null || p.getPid() == null || p.getPid().isEmpty()) {
                continue;
            }
            try {
                CuidadosEspecie cuidados = PlantBookService.detalhe(p.getPid());
                avisos.addAll(gerarAvisosPlanta(p, cuidados, meteo));
            } catch (Exception ignored) {
                // planta individual não deve bloquear as restantes
            }
        }
        return avisos;
    }

    // Gera os avisos meteorológicos para uma única planta
    public static List<Aviso> gerarAvisosPlanta(Planta p, CuidadosEspecie cuidados, MeteorologiaContainer meteo) {
        List<Aviso> avisosPlanta = new ArrayList<>();
        if (p == null || cuidados == null || meteo == null) {
            return avisosPlanta;
        }
        Aviso temperatura = Aviso.verificarTemperatura(p, cuidados, meteo);
        if (temperatura != null) {
            avisosPlanta.add(temperatura);
        }
        Aviso humidadeAlta = Aviso.verificarHumidadeSolo(p, cuidados, meteo);
        if (humidadeAlta != null) {
            avisosPlanta.add(humidadeAlta);
        }
        return avisosPlanta;
    }

    // Obtém a próxima rega prevista para uma única planta
    public static Aviso obterProximaRega(Planta p) throws Exception {
        if (p == null) {
            return null;
        }
        MeteorologiaContainer meteo = MeteorologiaContainer.getInstance();
        CuidadosEspecie cuidados = null;
        if (p.getPid() != null && !p.getPid().isEmpty()) {
            cuidados = PlantBookService.detalhe(p.getPid());
        }
        return Aviso.obterProximaRega(p, cuidados, meteo);
    }
}