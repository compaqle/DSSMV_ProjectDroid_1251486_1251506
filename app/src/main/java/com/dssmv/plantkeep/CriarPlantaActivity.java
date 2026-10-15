package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;
import com.dssmv.plantkeep.views.PlanViewAndroid;

/**
 * Ecrã de CRIAR planta (o formulário)
 */
public class CriarPlantaActivity extends AppCompatActivity {

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlantaContainer service = new PlantaContainer();   // o SERVICE
        PlanView view = new PlanViewAndroid(this);          // a VIEW do form
        controller = new Controller(service, view);         // o CONTROLLER (único)

        Button btnSalvar = findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(v -> controller.criar());
    }
}
