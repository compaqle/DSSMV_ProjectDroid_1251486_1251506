package com.dssmv.plantkeep.views;

import com.dssmv.plantkeep.model.Planta;

import java.util.List;

/**
 * A VIEW do padrão MVC (como a StudentView do professor).
 * O Controller manda-a fazer (mostraMensagem, mostraErro, atualizaLista)
 * e pede-lhe input (getPlantaInput). Implementada pela MainActivity.
 */
public interface PlanView {
    /** Recolhe os dados introduzidos pelo utilizador (como getStudent do professor). */
    Planta getPlantaInput();

    void mostraMensagem(String msg);
    void mostraErro(String msg);
    void atualizaLista(List<Planta> plantas);
    void limparCampos();
}
