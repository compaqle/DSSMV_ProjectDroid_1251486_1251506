package com.dssmv.plantkeep.views;

import android.app.Activity;
import android.widget.Toast;

import com.dssmv.plantkeep.model.Planta;

import java.util.List;

/** View do ecrã de menu: só mostra mensagens e erros. */
public class PlanViewMenu implements PlanView {
    private final Activity activity;

    public PlanViewMenu(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Planta getPlantaInput() { return null; }

    @Override
    public void mostraMensagem(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mostraErro(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void atualizaLista(List<Planta> plantas) { }

    @Override
    public void limparCampos() { }
}