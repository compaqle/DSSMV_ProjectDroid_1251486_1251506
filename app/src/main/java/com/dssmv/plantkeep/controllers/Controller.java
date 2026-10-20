package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.views.PlanView;
import com.dssmv.plantkeep.model.MeteorologiaContainer;

import java.util.List;


public class Controller {
    private final PlantaContainer service;   
    private final PlanView view;
    private final MeteorologiaContainer meteoService;


    public Controller(PlantaContainer service, PlanView view) {
        this.service = service;
        this.view = view;
        this.meteoService = MeteorologiaContainer.getInstance();
    }

    public void run() {
        listar();
    }

    public void criar() {
        service.criar(view.getPlantaInput(), new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                view.mostraMensagem("Planta guardada!");
                view.limparCampos();
            }

            @Override
            public void onErro(String msg) {
                view.mostraErro(msg);
            }
        });
    }

    public void listar() {
        service.listar(new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                view.atualizaLista(plantas);
            }

            @Override
            public void onErro(String msg) {
                view.mostraErro(msg);
            }
        });
    }

    /** Apaga: chama o service, vê o resultado. */
    public void apagar(final Planta p) {
        service.apagar(p, new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                view.mostraMensagem("Planta apagada!");
                view.atualizaLista(plantas);
            }

            @Override
            public void onErro(String msg) {
                view.mostraErro(msg);
            }
        });
    }


    public void atualizarMeteorologia(LocalizacaoProvider localizacao) {
        localizacao.obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                meteoService.carregarMeteorologia(latitude, longitude, new MeteorologiaContainer.Callback() {
                    @Override
                    public void onSucesso(MeteorologiaContainer container) {
                        view.mostraMensagem("Meteorologia atualizada!");
                    }

                    @Override
                    public void onErro(String msg) {
                        view.mostraErro(msg);
                    }
                });
            }

            @Override
            public void onErro(String msg) {
                view.mostraErro(msg);
            }
        });
    }
}
