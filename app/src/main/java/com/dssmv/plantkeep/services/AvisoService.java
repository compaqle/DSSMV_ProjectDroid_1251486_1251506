package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por comparar as plantas do utilizador com a meteorologia e obter a lista de avisos
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

    /**
     * Obtém a próxima rega prevista para uma única planta (Aviso HUMIDADE_SOLO_BAIXA)
     */
    public static Aviso obterProximaRega(Planta p) throws Exception {
        if (p == null || p.getPid() == null || p.getPid().isEmpty()) {
            return null;
        }
        MeteorologiaContainer meteo = MeteorologiaContainer.getInstance();
        DetalheOpb detalhe = PlantBookService.detalhe(p.getPid());
        // A decisão (meteorologia prevê seca, senão cadência normal)
        return Aviso.obterProximaRega(p, detalhe.getMinSoilMoist(), detalhe.getMaxSoilMoist(), meteo);
    }
}
