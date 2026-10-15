package com.dssmv.plantkeep.views;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Toast;

import com.dssmv.plantkeep.R;
import com.dssmv.plantkeep.model.Planta;

import java.util.List;

/**
 * A VIEW concreta do Android — como as *View do FSOFT (ClienteView, etc.).
 * Recebe a Activity para aceder à UI (widgets + runOnUiThread).
 * O Controller manda-a fazer e pede-lhe input.
 */
public class PlanViewAndroid implements PlanView {
    private final Activity activity;
    private final EditText edtNome;
    private final EditText edtEspecie;

    public PlanViewAndroid(Activity activity) {
        this.activity = activity;
        this.edtNome = activity.findViewById(R.id.edt_nome);
        this.edtEspecie = activity.findViewById(R.id.edt_especie);
    }

    /** Recolhe os dados dos campos (como getDadosCriarCliente do FSOFT). */
    @Override
    public Planta getPlantaInput() {
        String nome = edtNome.getText().toString().trim();
        String especie = edtEspecie.getText().toString().trim();
        return new Planta(nome, especie);
    }

    @Override
    public void mostraMensagem(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mostraErro(String msg) {
        activity.runOnUiThread(() -> Toast.makeText(activity, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void atualizaLista(List<Planta> plantas) {
        activity.runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            for (Planta p : plantas) {
                sb.append(p.getNome());
                if (p.getEspecie() != null && !p.getEspecie().isEmpty()) {
                    sb.append(" (").append(p.getEspecie()).append(")");
                }
                sb.append("\n");
            }
            Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void limparCampos() {
        activity.runOnUiThread(() -> {
            edtNome.setText("");
            edtEspecie.setText("");
        });
    }
}
