package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;
import com.dssmv.plantkeep.views.PlanViewLista;

/**
 * Ecrã de LISTA de plantas. É um "main" como a MainActivity: cria o service,
 * a view (desta vez a de lista) e o MESMO Controller, e manda correr —
 * que aqui lista. Usa o mesmo ficheiro Controller.java (único).
 */
public class PlantasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantas);

        PlantaContainer service = new PlantaContainer();
        PlanView view = new PlanViewLista(this);        // view da lista
        Controller controller = new Controller(service, view);
        controller.run();                                // lista ao abrir

        Button btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setOnClickListener(v -> finish());     // fecha este ecrã e volta
    }
}
