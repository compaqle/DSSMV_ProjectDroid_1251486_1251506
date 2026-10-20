package com.dssmv.plantkeep.model;

import com.dssmv.plantkeep.services.MeteoService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MeteorologiaContainer {

    private List<Meteorologia> previsaoHoraria;
    private final MeteoService meteoService;
    private static MeteorologiaContainer instancia;

    public interface Callback {
        void onSucesso(MeteorologiaContainer container);
        void onErro(String msg);

    }

    public MeteorologiaContainer() {
        this.previsaoHoraria = new ArrayList<>();
        this.meteoService = new MeteoService();
    }

    public MeteorologiaContainer(List<Meteorologia> previsaoHoraria) {
        this.previsaoHoraria = (previsaoHoraria != null) ? previsaoHoraria : new ArrayList<>();
        this.meteoService = new MeteoService();
    }

    public static synchronized MeteorologiaContainer getInstance() {
        if (instancia == null) {
            instancia = new MeteorologiaContainer();
        }
        return instancia;
    }

    public void carregarMeteorologia(double latitude, double longitude, Callback callback) {
        new Thread(() -> {
            MeteorologiaContainer resultado = meteoService.obterDadosMeteorologicos(latitude, longitude);
            if (resultado != null && !resultado.getPrevisaoHoraria().isEmpty()) {
                this.previsaoHoraria = resultado.getPrevisaoHoraria();
                callback.onSucesso(this);
            } else {
                callback.onErro("Erro ao obter dados meteorológicos");
            }
        }).start();
    }

    public double calcularPrecipitacaoPorDia(Date diaEscolhido) {
        if (diaEscolhido == null) {
            return -1;
        }
        double total = 0;
        boolean encontrouDados = false;

        Calendar calEscolhido = Calendar.getInstance();
        calEscolhido.setTime(diaEscolhido);
        Calendar calRegisto = Calendar.getInstance();

        for (Meteorologia registo : previsaoHoraria) {
            calRegisto.setTime(registo.getDataHora());

            boolean mesmoDia = calEscolhido.get(Calendar.DAY_OF_YEAR) == calRegisto.get(Calendar.DAY_OF_YEAR);

            if (mesmoDia) {
                total += registo.getPrecipitacao();
                encontrouDados = true;
            }
        }

        return encontrouDados ? total : -1;
    }

    public List<Meteorologia> getPrevisaoHoraria() {return previsaoHoraria;}
}