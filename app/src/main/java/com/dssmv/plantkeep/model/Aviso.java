package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Aviso {

    // Duração do efeito de uma rega (3 dias em milissegundos)
    private static final long EFEITO_REGA_MS = 3 * 24 * 60 * 60 * 1000;

    public enum TipoAviso {
        TEMPERATURA_BAIXA,
        TEMPERATURA_ALTA,
        HUMIDADE_SOLO_BAIXA,
        HUMIDADE_SOLO_ALTA
    }

    private final Planta planta;
    private final String nomePlanta;
    private final TipoAviso tipo;
    private final String mensagem;
    private final Date dataHora;

    public Aviso(Planta planta, TipoAviso tipo, String mensagem, Date dataHora) {
        if (planta == null || planta.getNome() == null || planta.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da planta inválido");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo é obrigatório");
        }
        if (dataHora == null) {
            throw new IllegalArgumentException("dataHora não pode ser nula");
        }
        this.planta = planta;
        this.nomePlanta = planta.getNome().trim();
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.dataHora = new Date(dataHora.getTime());
    }

    public Planta getPlanta() {
        return planta;
    }

    public TipoAviso getTipo() {
        return tipo;
    }

    public Date getDataHora() {
        return new Date(dataHora.getTime());
    }

    /** Decide se há aviso de temperatura */
    public static Aviso verificarTemperatura(Planta p, CuidadosEspecie cuidados,
                                             MeteorologiaContainer meteo) {
        if (cuidados == null || meteo == null) {
            return null;
        }
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        if (registoMin != null && !cuidados.isTemperaturaAdequada(registoMin.getTemperatura())
                && cuidados.isTemperaturaAbaixoDoMinimo(registoMin.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto (%.1f°C, Min: %d°C)", registoMin.getTemperatura(), cuidados.getMinTemp());
            return new Aviso(p, TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora());
        }

        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();
        if (registoMax != null && !cuidados.isTemperaturaAdequada(registoMax.getTemperatura())
                && cuidados.isTemperaturaAcimaDoMaximo(registoMax.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto (%.1f°C, Max: %d°C).", registoMax.getTemperatura(), cuidados.getMaxTemp());
            return new Aviso(p, TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora());
        }
        return null;
    }

    /** Decide se há aviso de humidade do solo */
    public static Aviso verificarHumidadeSolo(Planta p, CuidadosEspecie cuidados,
                                              MeteorologiaContainer meteo) {
        if (cuidados == null || meteo == null) {
            return null;
        }
        if (cuidados.getMinSoilMoist() != null) {
            long agoraMs = System.currentTimeMillis();
            // Se foi regada há menos de 3 dias, só procura necessidade de rega após esses 3 dias
            long fimEfeitoRegaMs = p.getUltimaRega() + EFEITO_REGA_MS;
            Date procurarAPartirDe = new Date(Math.max(agoraMs, fimEfeitoRegaMs));

            Meteorologia registoRega = meteo.obterPrimeiroRegistoAbaixoHumidadeMinima(
                    cuidados.getMinSoilMoist(), procurarAPartirDe);
            if (registoRega != null) {
                return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", registoRega.getDataHora());
            }
        }

        Meteorologia registoHumido = meteo.obterRegistoHumidadeSoloMaximaPrevista();
        if (registoHumido != null && cuidados.isHumidadeSoloAcimaDoMaximo(registoHumido.getHumidadeSolo())) {
            String msg = String.format(Locale.ROOT,
                    "Solo encharcado previsto (%.1f%%, Max: %d%%).",
                    registoHumido.getHumidadeSolo(), cuidados.getMaxSoilMoist());
            return new Aviso(p, TipoAviso.HUMIDADE_SOLO_ALTA, msg, registoHumido.getDataHora());
        }
        return null;
    }

    /** Próxima rega recomendada para uma planta */
    public static Aviso obterProximaRega(Planta p, CuidadosEspecie cuidados,
                                         MeteorologiaContainer meteo) {
        Aviso porMeteo = verificarHumidadeSolo(p, cuidados, meteo);
        if (porMeteo != null && porMeteo.getTipo() == TipoAviso.HUMIDADE_SOLO_BAIXA) {
            return porMeteo; // meteorologia prevê seca -> regar nesse dia
        }
        // sem seca prevista: próxima rega pela cadência normal (efeito de uma rega = 3 dias)
        long base = p.getUltimaRega();
        long alvo = base + EFEITO_REGA_MS;
        if (base <= 0 || alvo <= System.currentTimeMillis()) {
            return null; // nunca regada ou cadência já passou -> nada a agendar
        }
        return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", new Date(alvo));
    }

    public String formatarData() {
        long diffMs = dataHora.getTime() - System.currentTimeMillis();
        if (diffMs <= 0) {
            return "hoje";
        }
        // Calcula os dias arredondando para cima (ex: 72 horas = 3 dias)
        long dias = (long) Math.ceil(diffMs / (1000.0 * 60 * 60 * 24));

        if (dias <= 0) {
            return "hoje";
        } else if (dias == 1) {
            return "em 1 dia";
        } else {
            return "em " + dias + " dias";
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (tipo == TipoAviso.HUMIDADE_SOLO_BAIXA) {
            return nomePlanta + " " + formatarData();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
        return nomePlanta + " (" + sdf.format(dataHora) + "): " + mensagem;
    }
}