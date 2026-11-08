package com.dssmv.plantkeep.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Aviso {

    private static final long EFEITO_REGA_MS = 3 * 24 * 60 * 60 * 1000L; // 3 dias em milissegundos

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

    // Verifica se há previsão de frio e/ou calor fora dos limites da planta
    public static List<Aviso> verificarTemperatura(Planta p, CuidadosEspecie cuidados, MeteorologiaContainer meteo) {
        List<Aviso> avisos = new ArrayList<>();
        if (p == null || cuidados == null || meteo == null) {
            return avisos;
        }
        Meteorologia registoMin = meteo.obterRegistoTemperaturaMinimaPrevista();
        if (registoMin != null && cuidados.isTemperaturaAbaixoDoMinimo(registoMin.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Frio previsto (%.1f °C, Min: %d °C)", registoMin.getTemperatura(), cuidados.getMinTemp());
            avisos.add(new Aviso(p, TipoAviso.TEMPERATURA_BAIXA, msg, registoMin.getDataHora()));
        }
        Meteorologia registoMax = meteo.obterRegistoTemperaturaMaximaPrevista();
        if (registoMax != null && cuidados.isTemperaturaAcimaDoMaximo(registoMax.getTemperatura())) {
            String msg = String.format(Locale.ROOT,
                    "Calor previsto (%.1f °C, Max: %d °C).", registoMax.getTemperatura(), cuidados.getMaxTemp());
            avisos.add(new Aviso(p, TipoAviso.TEMPERATURA_ALTA, msg, registoMax.getDataHora()));
        }
        return avisos;
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
        long agora = System.currentTimeMillis();
        long fimEfeitoRegaMs = p.getUltimaRega() + EFEITO_REGA_MS;

        // Se temos previsão meteorológica e limite de humidade, decidimos com base no solo
        if (cuidados != null && cuidados.getMinSoilMoist() != null
                && meteo != null && !meteo.getPrevisaoHoraria().isEmpty()) {
            Date procurarAPartirDe = new Date(Math.max(agora, fimEfeitoRegaMs));
            Meteorologia registoRega = meteo.obterPrimeiroRegistoAbaixoHumidadeMinima(
                    cuidados.getMinSoilMoist(), procurarAPartirDe);
            if (registoRega != null) {
                return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", registoRega.getDataHora());
            }
            // Solo mantém-se húmido nos próximos dias (ex: chuva prevista)
            return null;
        }

        // Sem meteorologia disponível, usa a regra simples de regar a cada 3 dias
        if (p.getUltimaRega() <= 0 || fimEfeitoRegaMs <= agora) {
            return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", new Date(agora));
        }
        return new Aviso(p, TipoAviso.HUMIDADE_SOLO_BAIXA, "", new Date(fimEfeitoRegaMs));
    }

    // Formata a distância em dias de calendário (hoje, em 1 dia, em X dias)
    public String formatarData() {
        long hojeMeiaNoite = inicioDoDiaMs(new Date());
        long dataAvisoMeiaNoite = inicioDoDiaMs(dataHora);
        long diffMs = dataAvisoMeiaNoite - hojeMeiaNoite;

        if (diffMs <= 0) {
            return "hoje";
        }
        long dias = Math.round(diffMs / (1000.0 * 60 * 60 * 24));
        return (dias == 1) ? "em 1 dia" : "em " + dias + " dias";
    }

    private static long inicioDoDiaMs(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    public String toString() {
        if (tipo == TipoAviso.HUMIDADE_SOLO_BAIXA) {
            return nomePlanta + " " + formatarData();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM 'às' HH:mm", Locale.getDefault());
        return nomePlanta + " (" + sdf.format(dataHora) + "): " + mensagem;
    }
}