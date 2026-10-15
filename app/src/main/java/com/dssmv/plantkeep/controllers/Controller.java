package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;

import java.util.List;


public class Controller {
    private final PlantaContainer service;   
    private final PlanView view;            

    public Controller(PlantaContainer service, PlanView view) {
        this.service = service;
        this.view = view;
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
}
