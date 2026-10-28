package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Serviço responsável por cruzar as plantas do utilizador com a meteorologia
 * e gerar a lista de avisos (Temperatur e Humidade do Solo).
 */
public class AvisoService {

    private AvisoService() {}

    /** Gera todos os avisos ativos para as plantas do utilizador. */
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

                // Verificação de Temperatura
                verificarAvisosTemperatura(p, detalhe, meteo, avisos);
                // Verificação de Humidade do Solo
                verificarAvisosHumidadeSolo(p, detalhe, meteo, avisos);

            } catch (Exception ignored) {
            }
        }

        return avisos;
    }

    /** Verifica se a temperatura prevista ultrapassa o minTemp ou maxTemp da planta. */
    private static void verificarAvisosTemperatura(Planta p, DetalheOpb detalhe, MeteorologiaContainer meteo, List<Aviso> avisos) {
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();

        if (detalhe.getMinTemp() != null && registoMin != null
                && registoMin.getTemperatura() < detalhe.getMinTemp()) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto: %.1f°C. Mínimo suportado: %d°C",
                    registoMin.getTemperatura(), detalhe.getMinTemp());
            avisos.add(new Aviso(p.getNome(), Aviso.TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora()));
        }

        if (detalhe.getMaxTemp() != null && registoMax != null
                && registoMax.getTemperatura() > detalhe.getMaxTemp()) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto: %.1f°C. Máximo suportado: %d°C",
                    registoMax.getTemperatura(), detalhe.getMaxTemp());
            avisos.add(new Aviso(p.getNome(), Aviso.TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora()));
        }
    }

    private static void verificarAvisosHumidadeSolo(Planta p, DetalheOpb detalhe,
                                                    MeteorologiaContainer meteo, List<Aviso> avisos) {
        Meteorologia registoSeco = meteo.obterRegistoHumidadeSoloMinimaPrevista();
        Meteorologia registoHumido = meteo.obterRegistoHumidadeSoloMaximaPrevista();

        if (detalhe.getMinSoilMoist() != null && registoSeco != null
                && registoSeco.getHumidadeSolo() < detalhe.getMinSoilMoist()) {
            String msg = String.format(Locale.ROOT,
                    "Humidade do solo prevista: %.1f%%. Mínimo recomendado: %d%%",
                    registoSeco.getHumidadeSolo(), detalhe.getMinSoilMoist());
            avisos.add(new Aviso(p.getNome(), Aviso.TipoAviso.HUMIDADE_SOLO_BAIXA, msg, registoSeco.getDataHora()));
        }

        if (detalhe.getMaxSoilMoist() != null && registoHumido != null
                && registoHumido.getHumidadeSolo() > detalhe.getMaxSoilMoist()) {
            String msg = String.format(Locale.ROOT,
                    "Humidade do solo prevista: %.1f%%. Máximo recomendado: %d%%",
                    registoHumido.getHumidadeSolo(), detalhe.getMaxSoilMoist());
            avisos.add(new Aviso(p.getNome(), Aviso.TipoAviso.HUMIDADE_SOLO_ALTA, msg, registoHumido.getDataHora()));
        }
    }
}