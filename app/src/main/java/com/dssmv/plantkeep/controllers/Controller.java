package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;

import java.util.List;

/**
 * O CONTROLLER no estilo FSOFT: NÃO tem lógica dentro.
 * Só orquestra — pede input à view, chama o service (PlantaContainer)
 * e, quando o service responde, manda a view mostrar o resultado.
 */
public class Controller {
    private final PlantaContainer service;   // o SERVICE (FSOFT) — faz o trabalho
    private final PlanView view;             // a VIEW

    public Controller(PlantaContainer service, PlanView view) {
        this.service = service;
        this.view = view;
    }

    /** Ponto de partida (como run() do FSOFT). Arranca listando. */
    public void run() {
        listar();
    }

    /** Cria: pede a planta à view, chama o service, vê o resultado. */
    public void criar() {
        service.criar(view.getPlantaInput(), new PlantaContainer.Callback() {
            @Override
            public void onSucesso(List<Planta> plantas) {
                view.mostraMensagem("Planta guardada!");
                view.limparCampos();
                view.atualizaLista(plantas);
            }

            @Override
            public void onErro(String msg) {
                view.mostraErro(msg);
            }
        });
    }

    /** Lista: chama o service e mostra o resultado. */
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
