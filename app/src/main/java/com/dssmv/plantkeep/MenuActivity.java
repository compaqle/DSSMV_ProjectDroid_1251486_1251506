package com.dssmv.plantkeep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
MENU PRINCIPAL
 */
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnCriar = findViewById(R.id.btn_criar_planta);
        btnCriar.setOnClickListener(v ->
                startActivity(new Intent(this, CriarPlantaActivity.class)));

        Button btnVer = findViewById(R.id.btn_ver_plantas);
        btnVer.setOnClickListener(v ->
                startActivity(new Intent(this, PlantasActivity.class)));
    }
}
