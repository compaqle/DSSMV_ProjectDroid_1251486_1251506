package com.dssmv.plantkeep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.model.RegistoDiario;
import com.dssmv.plantkeep.services.PlantaService;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ecrã do Diário da Planta: permite reportar o estado de crescimento (texto),
 * tirar fotografias com a câmara do telemóvel e guardar tudo no RestDB.
 */
public class DiarioPlantaActivity extends AppCompatActivity {

    private String plantaId;
    private EditText edtRegisto;
    private ImageView imgPreview;
    private Button btnTirarFoto;
    private Button btnAdicionar;
    private TextView txtEstadoDiario;
    private ListView lvDiario;

    private String fotoAtualBase64 = "";

    // Abre a câmara e recebe o Bitmap da fotografia tirada
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    fotoAtualBase64 = bitmapParaBase64(bitmap);
                    imgPreview.setImageBitmap(bitmap);
                    imgPreview.setVisibility(View.VISIBLE);
                }
            }
    );

    // Pede permissão de CAMERA em runtime (Semana 5 - T5)
    private final ActivityResultLauncher<String> permissaoCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            concedida -> {
                if (concedida) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Permissão da câmara negada.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario_planta);

        plantaId = getIntent().getStringExtra("id");
        String nomePlanta = getIntent().getStringExtra("nome");

        TextView txtTitulo = findViewById(R.id.txt_titulo_diario);
        edtRegisto = findViewById(R.id.edt_registo_diario);
        imgPreview = findViewById(R.id.img_preview_diario);
        btnTirarFoto = findViewById(R.id.btn_tirar_foto);
        btnAdicionar = findViewById(R.id.btn_adicionar_registo);
        txtEstadoDiario = findViewById(R.id.txt_estado_diario);
        lvDiario = findViewById(R.id.lv_diario);
        Button btnVoltar = findViewById(R.id.btn_voltar_diario);

        if (nomePlanta != null && !nomePlanta.isEmpty()) {
            txtTitulo.setText("Diário: " + nomePlanta);
        }

        btnTirarFoto.setOnClickListener(v -> verificarPermissaoEAbrirCamera());
        btnAdicionar.setOnClickListener(v -> adicionarRegisto());
        btnVoltar.setOnClickListener(v -> finish());

        carregarDiario();
    }

    private void verificarPermissaoEAbrirCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissaoCameraLauncher.launch(Manifest.permission.CAMERA);
        } else {
            cameraLauncher.launch(null);
        }
    }

    private void carregarDiario() {
        txtEstadoDiario.setVisibility(View.VISIBLE);
        txtEstadoDiario.setText("A carregar registos...");

        new Thread(() -> {
            try {
                final Planta p = PlantaService.obterPlantaPorId(plantaId);
                runOnUiThread(() -> mostrarRegistos(p != null ? p.getDiario() : null));
            } catch (Exception e) {
                runOnUiThread(() -> txtEstadoDiario.setText("Erro ao carregar diário: " + e.getMessage()));
            }
        }).start();
    }

    private void adicionarRegisto() {
        final String texto = edtRegisto.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Escreve o estado de crescimento da planta.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String fotoParaEnviar = fotoAtualBase64;
        btnAdicionar.setEnabled(false);
        btnTirarFoto.setEnabled(false);

        new Thread(() -> {
            try {
                final Planta pAtualizada = PlantaService.adicionarRegistoDiario(plantaId, texto, fotoParaEnviar);
                runOnUiThread(() -> {
                    btnAdicionar.setEnabled(true);
                    btnTirarFoto.setEnabled(true);
                    edtRegisto.setText("");
                    fotoAtualBase64 = "";
                    imgPreview.setImageBitmap(null);
                    imgPreview.setVisibility(View.GONE);
                    Toast.makeText(DiarioPlantaActivity.this, "Registo guardado no diário!", Toast.LENGTH_SHORT).show();
                    mostrarRegistos(pAtualizada.getDiario());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAdicionar.setEnabled(true);
                    btnTirarFoto.setEnabled(true);
                    Toast.makeText(DiarioPlantaActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarRegistos(List<RegistoDiario> registos) {
        if (registos == null || registos.isEmpty()) {
            txtEstadoDiario.setVisibility(View.VISIBLE);
            txtEstadoDiario.setText("Ainda não existem registos no diário desta planta.");
            lvDiario.setAdapter(null);
            return;
        }

        final List<RegistoDiario> ordenados = new ArrayList<>(registos);
        // Ordena do registo mais recente para o mais antigo
        ordenados.sort((r1, r2) -> r2.getDataHora().compareTo(r1.getDataHora()));

        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());

        txtEstadoDiario.setVisibility(View.GONE);
        lvDiario.setAdapter(new ArrayAdapter<RegistoDiario>(this, 0, ordenados) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(DiarioPlantaActivity.this)
                            .inflate(R.layout.item_diario, parent, false);
                }

                RegistoDiario r = getItem(position);
                TextView txtData = convertView.findViewById(R.id.txt_data_diario);
                TextView txtTexto = convertView.findViewById(R.id.txt_texto_diario);
                ImageView imgFoto = convertView.findViewById(R.id.img_registo_diario);

                if (r != null) {
                    txtData.setText(sdf.format(r.getDataHora()));
                    txtTexto.setText(r.getTexto());

                    Bitmap bmp = base64ParaBitmap(r.getFotoBase64());
                    if (bmp != null) {
                        imgFoto.setImageBitmap(bmp);
                        imgFoto.setVisibility(View.VISIBLE);
                    } else {
                        imgFoto.setImageBitmap(null);
                        imgFoto.setVisibility(View.GONE);
                    }
                }
                return convertView;
            }
        });
    }

    /** Comprime o Bitmap em JPEG e converte para String Base64 para guardar no JSON do RestDB. */
    private static String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /** Converte a String Base64 de volta para Bitmap (ou null se não tiver foto). */
    private static Bitmap base64ParaBitmap(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}