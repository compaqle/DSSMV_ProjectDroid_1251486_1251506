package com.dssmv.plantkeep.services;

import com.dssmv.plantkeep.dto.DetalheOpb;
import com.dssmv.plantkeep.model.Aviso;
import com.dssmv.plantkeep.model.Meteorologia;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Serviço responsável por cruzar as plantas do utilizador com a meteorologia
 * e gerar a lista de avisos (Temperatura e Humidade do Solo).
 */
public class AvisoService {

    // Duração do efeito de uma rega manual (3 dias em milissegundos)
    private static final long EFEITO_REGA_MS = 3L * 24 * 60 * 60 * 1000;

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

                verificarAvisosTemperatura(p, detalhe, meteo, avisos);
                verificarAvisosHumidadeSolo(p, detalhe, meteo, avisos);

            } catch (Exception ignored) {
            }
        }

        return avisos;
    }

    /** Verifica se a temperatura prevista ultrapassa o minTemp ou maxTemp da planta. */
    private static void verificarAvisosTemperatura(Planta p, DetalheOpb detalhe,
                                                   MeteorologiaContainer meteo, List<Aviso> avisos) {
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();

        if (detalhe.getMinTemp() != null && registoMin != null
                && registoMin.getTemperatura() < detalhe.getMinTemp()) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto (%.1f°C, Min: %d°C)",
                    registoMin.getTemperatura(), detalhe.getMinTemp());
            avisos.add(new Aviso(p, Aviso.TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora()));
        }

        if (detalhe.getMaxTemp() != null && registoMax != null
                && registoMax.getTemperatura() > detalhe.getMaxTemp()) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto (%.1f°C, Max: %d°C).",
                    registoMax.getTemperatura(), detalhe.getMaxTemp());
            avisos.add(new Aviso(p, Aviso.TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora()));
        }
    }

    /** Verifica quando a planta precisará de rega (considerando a última rega) ou se o solo ficará encharcado. */
    private static void verificarAvisosHumidadeSolo(Planta p, DetalheOpb detalhe,
                                                    MeteorologiaContainer meteo, List<Aviso> avisos) {
        if (detalhe.getMinSoilMoist() != null) {
            long agoraMs = System.currentTimeMillis();
            long fimEfeitoRegaMs = p.getUltimaRega() + EFEITO_REGA_MS;

            // Se foi regada há menos de 3 dias, só procura necessidade de rega após esses 3 dias
            Date procurarAPartirDe = new Date(Math.max(agoraMs, fimEfeitoRegaMs));

            Meteorologia registoRega = meteo.obterPrimeiroRegistoAbaixoHumidadeMinima(
                    detalhe.getMinSoilMoist(), procurarAPartirDe);

            if (registoRega != null) {
                avisos.add(new Aviso(p, Aviso.TipoAviso.HUMIDADE_SOLO_BAIXA, "", registoRega.getDataHora()));
            }
        }

        Meteorologia registoHumido = meteo.obterRegistoHumidadeSoloMaximaPrevista();
        if (detalhe.getMaxSoilMoist() != null && registoHumido != null
                && registoHumido.getHumidadeSolo() > detalhe.getMaxSoilMoist()) {
            String msg = String.format(Locale.ROOT,
                    "Solo encharcado previsto (%.1f%%, Max: %d%%).",
                    registoHumido.getHumidadeSolo(), detalhe.getMaxSoilMoist());
            avisos.add(new Aviso(p, Aviso.TipoAviso.HUMIDADE_SOLO_ALTA, msg, registoHumido.getDataHora()));
        }
    }
}