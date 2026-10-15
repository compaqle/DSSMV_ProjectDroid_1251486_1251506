package com.dssmv.plantkeep;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.model.PlantaContainer;
import com.dssmv.plantkeep.views.PlanView;
import com.dssmv.plantkeep.views.PlanViewAndroid;

/**
 * A MainActivity é o "main" do FSOFT: ponto de entrada que cria os componentes
 * (service, view, controller) e liga os botões. NÃO é a view — a view é a
 * PlanViewAndroid. O controller.run() arranca a app.
 */
public class MainActivity extends AppCompatActivity {

    private Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Como o main() do FSOFT: cria service, view e controller, e manda correr.
        PlantaContainer service = new PlantaContainer();   // o SERVICE
        PlanView view = new PlanViewAndroid(this);          // a VIEW (classe própria)
        controller = new Controller(service, view);         // o CONTROLLER
        controller.run();                                    // arranca

        Button btnSalvar = findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(v -> controller.criar());

        Button btnListar = findViewById(R.id.btn_listar);
        btnListar.setOnClickListener(v -> controller.listar());
    }
}
