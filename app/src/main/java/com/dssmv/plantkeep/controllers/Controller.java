package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.model.MeteorologiaContainer;
import com.dssmv.plantkeep.services.LocalizacaoProvider;
import com.dssmv.plantkeep.views.PlanView;

import java.util.List;

/**
 * O controller recebe so o model guarda a view e orquestra
 */
public class Controller {
    private PlantaContainer model;          // o model
    private PlanView view;                  // a view
    private MeteorologiaContainer meteoService;

    /** Recebe so o model. A view entra depois. */
    public Controller(PlantaContainer model) {
        this.model = model;
        this.meteoService = MeteorologiaContainer.getInstance();
    }

    /** A Activity (main) dá a view concreta. */
    public void setView(PlanView view) {
        this.view = view;
    }

    /** Entrada: lista logo ao abrir. */
    public void run() {
        listar();
    }

    /** Pede a planta a view e manda criar no model */
    public void criar() {
        Planta planta = this.view.getPlantaInput();
        this.model.criar(planta, new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                Controller.this.view.mostraMensagem("Planta guardada!");
                Controller.this.view.limparCampos();
            }

            @Override
            public void onErro(String msg) {
                Controller.this.view.mostraErro(msg);
            }
        });
    }

    /** Pede ao model e manda mostrar na view */
    public void listar() {
        this.model.listar(new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                Controller.this.view.atualizaLista(plantas);
            }

            @Override
            public void onErro(String msg) {
                Controller.this.view.mostraErro(msg);
            }
        });
    }

    /** Chama o model e vê o resultado. */
    public void apagar(final Planta p) {
        this.model.apagar(p, new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                Controller.this.view.mostraMensagem("Planta apagada!");
                Controller.this.view.atualizaLista(plantas);
            }

            @Override
            public void onErro(String msg) {
                Controller.this.view.mostraErro(msg);
            }
        });
    }

    /** Pede a localização e manda carregar no model. */
    public void atualizarMeteorologia(LocalizacaoProvider localizacao) {
        localizacao.obterLocalizacao(new LocalizacaoProvider.Callback() {
            @Override
            public void onLocalizacao(double latitude, double longitude) {
                meteoService.carregarMeteorologia(latitude, longitude, new MeteorologiaContainer.Callback() {
                    @Override
                    public void onSucesso(MeteorologiaContainer container) {
                        Controller.this.view.mostraMensagem("Meteorologia atualizada!");
                    }

                    @Override
                    public void onErro(String msg) {
                        Controller.this.view.mostraErro(msg);
                    }
                });
            }

            @Override
            public void onErro(String msg) {
                Controller.this.view.mostraErro(msg);
            }
        });
    }
}
