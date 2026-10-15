package com.dssmv.plantkeep.controllers;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;

import java.util.List;

/**
 * O CONTROLLER do padrão MVC, como o do professor: tem o model e a view,
 * e só orquestra. Recolhe input da view, manda o model operar e manda a
 * view mostrar o resultado.
 */
public class Controller {
    private final PlantaContainer model;   // o MODEL (container das plantas)
    private final PlanView view;           // a VIEW

    public Controller(PlantaContainer model, PlanView view) {
        this.model = model;
        this.view = view;
    }

    /** Ponto de partida — como o run() do professor. Arranca listando as plantas. */
    public void run() {
        listar();
    }

    /** Recolhe a planta da view, manda o model guardar e manda a view confirmar. */
    public void criar() {
        new Thread(() -> {
            try {
                Planta p = view.getPlantaInput();       // 1. VIEW recolhe input
                model.add(p);                            // 2. MODEL guarda
                view.mostraMensagem("Planta guardada!"); // 3. VIEW confirma
                view.limparCampos();
                listar();
            } catch (Exception e) {
                view.mostraErro(e.getMessage());
            }
        }).start();
    }

    /** Manda o model devolver as plantas e a view apresentá-las. */
    public void listar() {
        new Thread(() -> {
            try {
                List<Planta> plantas = model.getAll();  // 1. MODEL devolve
                view.atualizaLista(plantas);             // 2. VIEW mostra
            } catch (Exception e) {
                view.mostraErro(e.getMessage());
            }
        }).start();
    }

    /** Manda o model apagar e a view confirmar. */
    public void apagar(final Planta p) {
        new Thread(() -> {
            try {
                model.remove(p);                          // 1. MODEL apaga
                view.mostraMensagem("Planta apagada!");   // 2. VIEW confirma
                listar();
            } catch (Exception e) {
                view.mostraErro(e.getMessage());
            }
        }).start();
    }
}
