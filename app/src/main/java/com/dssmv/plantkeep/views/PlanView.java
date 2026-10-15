package com.dssmv.plantkeep.views;

import com.dssmv.plantkeep.model.Planta;

import java.util.List;

/**
 * A VIEW do padrão MVC 
 */
public interface PlanView {
    Planta getPlantaInput();

    void mostraMensagem(String msg);
    void mostraErro(String msg);
    void atualizaLista(List<Planta> plantas);
    void limparCampos();
}
