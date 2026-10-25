package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;
import com.dssmv.plantkeep.views.PlanViewLista;

/**
 * Ecrã que mostra a lista de plante faz de main
 */
public class PlantasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantas);

        PlantaContainer service = new PlantaContainer();
        Controller controller = new Controller(service);
        controller.setView(new PlanViewLista(this));     // a view da lista
        controller.run();                                // listta ao abrir

        Button btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> finish());     // fecha e volta
    }
}
