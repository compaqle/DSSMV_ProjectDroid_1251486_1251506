package com.dssmv.plantkeep;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dssmv.plantkeep.model.PlantaContainer;
import androidx.appcompat.app.AppCompatActivity;
import com.dssmv.plantkeep.services.LocalizacaoService;
import com.dssmv.plantkeep.controllers.Controller;
import com.dssmv.plantkeep.views.PlanViewMenu;


/**
MENU PRINCIPAL
 */
public class MenuActivity extends AppCompatActivity {

    private Controller controller;
    private static final int CODIGO_PERMISSAO_GPS = 100;

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

        //Envia o pedido de permmissão para usar o GPS
        controller = new Controller(new PlantaContainer(), new PlanViewMenu(this));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODIGO_PERMISSAO_GPS);
        } else {
            controller.atualizarMeteorologia(new LocalizacaoService(this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISSAO_GPS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            controller.atualizarMeteorologia(new LocalizacaoService(this));
        } else {
            MenuActivity.this.mostraErro("Permissão de GPS negada.");
        }
    }

    private void mostraErro(String msg) {
        runOnUiThread(() -> Toast.makeText(MenuActivity.this, "Erro: " + msg, Toast.LENGTH_SHORT).show());
    }
}