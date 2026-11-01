package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por cruzar as plantas do utilizador com a meteorologia
 * e obter a lista de avisos (Temperatura e Humidade do Solo).
 * Apenas orquestra os dados: a decisão de "quando disparar um aviso"
 * vive no model (Aviso.verificarTemperatura / Aviso.verificarHumidadeSolo).
 */
public class AvisoService {

    private AvisoService() {}

    /** Obtém todos os avisos ativos para as plantas do utilizador. */
    public static List<Aviso> obterAvisosAtuais() throws Exception {
        List<Aviso> avisos = new ArrayList<>();
        MeteorologiaContainer meteo = MeteorologiaContainer.getInstance();

        if (meteo.getPrevisaoHoraria().isEmpty()) {
            return avisos;
        }

        List<Planta> plantas = PlantaService.getPlantas();

        for (Planta p : plantas) {
            if (p.getPid() == null || p.getPid().isEmpty()) {
                continue;
            }
            try {
                DetalheOpb detalhe = PlantBookService.detalhe(p.getPid());

                Aviso temperatura = Aviso.verificarTemperatura(
                        p, detalhe.getMinTemp(), detalhe.getMaxTemp(), meteo);
                if (temperatura != null) {
                    avisos.add(temperatura);
                }

                Aviso humidade = Aviso.verificarHumidadeSolo(
                        p, detalhe.getMinSoilMoist(), detalhe.getMaxSoilMoist(), meteo);
                if (humidade != null) {
                    avisos.add(humidade);
                }

            } catch (Exception ignored) {
                // planta individual não deve bloquear as restantes
            }
        }

        return avisos;
    }
}
