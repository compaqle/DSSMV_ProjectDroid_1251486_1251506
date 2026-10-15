package com.dssmv.plantkeep.views;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dssmv.plantkeep.R;
import com.dssmv.plantkeep.model.Planta;

import java.util.ArrayList;
import java.util.List;

/**
 * View concreta do ecrã de LISTA de plantas 
 */
public class PlanViewLista implements PlanView {
    private final Activity activity;
    private final ListView lvPlantas;

    public PlanViewLista(Activity activity) {
        this.activity = activity;
        this.lvPlantas = activity.findViewById(R.id.lv_plantas);
    }

    /** Não se introduzem plantas neste ecrã. */
    @Override
    public Planta getPlantaInput() {
        return null;
    }

    @Override
    public void mostraMensagem(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mostraErro(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }

    /** Preenche a ListView com os nomes das plantas (em vez de Toast). */
    @Override
    public void atualizaLista(List<Planta> plantas) {
        activity.runOnUiThread(() -> {
            List<String> nomes = new ArrayList<>();
            for (Planta p : plantas) {
                if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
                    nomes.add(p.getNome() + " (" + p.getEspecie() + ")");
                } else {
                    nomes.add(p.getNome());
                }
            }
            lvPlantas.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, nomes));
        });
    }

    /** Não há campos para limpar neste ecrã. */
    @Override
    public void limparCampos() {
        // sem campos
    }
}
