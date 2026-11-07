package com.dssmv.plantkeep.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Aviso {
    private static final long EFEITO_REGA_MS = 3 * 24 * 60 * 60 * 1000;  // 3 dias em milissegundos

    public enum TipoAviso {
        TEMPERATURA_BAIXA,
        TEMPERATURA_ALTA,
        HUMIDADE_SOLO_BAIXA,
        HUMIDADE_SOLO_ALTA
    }

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
        this.nomePlanta = planta.getNome().trim();
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.dataHora = new Date(dataHora.getTime());
    }

    public Date getDataHora() {
        return new Date(dataHora.getTime());
    }

    // Verifica se há previsão de frio ou calor fora dos limites da planta
    public static Aviso verificarTemperatura(Planta p, CuidadosEspecie cuidados, MeteorologiaContainer meteo) {
        if (cuidados == null || meteo == null) {
            return null;
        }
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        if (registoMin != null && cuidados.isTemperaturaAbaixoDoMinimo(registoMin.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto (%.1f°C, Min: %d°C)", registoMin.getTemperatura(), cuidados.getMinTemp());
            return new Aviso(p, TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora());
        }

        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();
        if (registoMax != null && cuidados.isTemperaturaAcimaDoMaximo(registoMax.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto (%.1f°C, Max: %d°C).", registoMax.getTemperatura(), cuidados.getMaxTemp());
            return new Aviso(p, TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora());
        }
        return null;
    }

    // Verifica se há previsão de excesso de humidade no solo
    public static Aviso verificarHumidadeSolo(Planta p, CuidadosEspecie cuidados, MeteorologiaContainer meteo) {
        if (p == null || cuidados == null || meteo == null) {
            return null;
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

    // Calcula quando a planta volta a precisar de ser regada
    public static Aviso obterProximaRega(Planta p, CuidadosEspecie cuidados, MeteorologiaContainer meteo) {
        if (p == null) {
            return null;
        }
        long fimEfeitoRegaMs = p.getUltimaRega() + EFEITO_REGA_MS;

        if (cuidados != null && meteo != null && cuidados.getMinSoilMoist() != null) {
            Date procurarAPartirDe = new Date(Math.max(System.currentTimeMillis(), fimEfeitoRegaMs));
            Meteorologia registoRega = meteo.obterPrimeiroRegistoAbaixoHumidadeMinima(
                    cuidados.getMinSoilMoist(), procurarAPartirDe);
            if (registoRega != null) {
                return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", registoRega.getDataHora());
            }
        }

        // Se não houver seca rega de x em x dias
        if (p.getUltimaRega() <= 0 || fimEfeitoRegaMs <= System.currentTimeMillis()) {
            return null;
        }
        return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", new Date(fimEfeitoRegaMs));
    }

    public String formatarData() {
        long diffMs = dataHora.getTime() - System.currentTimeMillis();
        if (diffMs <= 0) {
            return "hoje";
        }
        long dias = (long) Math.ceil(diffMs / (1000.0 * 60 * 60 * 24));
        return (dias == 1) ? "em 1 dia" : "em " + dias + " dias";
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