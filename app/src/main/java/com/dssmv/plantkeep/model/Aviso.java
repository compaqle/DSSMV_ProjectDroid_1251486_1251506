package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model de um Aviso/Alerta para uma planta, incluindo a data e hora do evento.
 * Contém também as decisões de negócio (verificarTemperatura/HumidadeSolo):
 * a lógica de "quando disparar um aviso" vive aqui, não no service.
 */
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

    /** Decide se há aviso de temperatura: compara a previsão do container com os limites da espécie. */
    public static Aviso verificarTemperatura(Planta p, Integer minTemp, Integer maxTemp,
                                             MeteorologiaContainer meteo) {
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();

        if (minTemp != null && registoMin != null && registoMin.getTemperatura() < minTemp) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto (%.1f°C, Min: %d°C)", registoMin.getTemperatura(), minTemp);
            return new Aviso(p, TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora());
        }

        if (maxTemp != null && registoMax != null && registoMax.getTemperatura() > maxTemp) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto (%.1f°C, Max: %d°C).", registoMax.getTemperatura(), maxTemp);
            return new Aviso(p, TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora());
        }
        return null;
    }

    /** Decide se há aviso de humidade do solo (rega necessária ou solo encharcado). Devolve Aviso ou null. */
    public static Aviso verificarHumidadeSolo(Planta p, Integer minSoilMoist, Integer maxSoilMoist,
                                              MeteorologiaContainer meteo) {
        if (minSoilMoist != null) {
            long agoraMs = System.currentTimeMillis();
            // Se foi regada há menos de 3 dias, só procura necessidade de rega após esses 3 dias
            long fimEfeitoRegaMs = p.getUltimaRega() + EFEITO_REGA_MS;
            Date procurarAPartirDe = new Date(Math.max(agoraMs, fimEfeitoRegaMs));

            Meteorologia registoRega = meteo.obterPrimeiroRegistoAbaixoHumidadeMinima(
                    minSoilMoist, procurarAPartirDe);
            if (registoRega != null) {
                return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", registoRega.getDataHora());
            }
        }

        Meteorologia registoHumido = meteo.obterRegistoHumidadeSoloMaximaPrevista();
        if (maxSoilMoist != null && registoHumido != null && registoHumido.getHumidadeSolo() > maxSoilMoist) {
            String msg = String.format(Locale.ROOT,
                    "Solo encharcado previsto (%.1f%%, Max: %d%%).", registoHumido.getHumidadeSolo(), maxSoilMoist);
            return new Aviso(p, TipoAviso.HUMIDADE_SOLO_ALTA, msg, registoHumido.getDataHora());
        }
        return null;
    }

    /**
     * Próxima rega recomendada para uma planta. Se a meteorologia previr seca
     * (solo abaixo do mínimo), devolve a data dessa rega
     */
    public static Aviso obterProximaRega(Planta p, Integer minSoilMoist, Integer maxSoilMoist,
                                         MeteorologiaContainer meteo) {
        Aviso porMeteo = verificarHumidadeSolo(p, minSoilMoist, maxSoilMoist, meteo);
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