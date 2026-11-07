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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ecrã do Diário da Planta
 * Permite adicionar registos ao diário e tirar fotografias com a câmara do telemóvel
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

    // Cache thread-safe para bitmaps descodificados fora da Main Thread
    private final Map<String, Bitmap> cacheFotos = new ConcurrentHashMap<>();

    // Abre a câmara e fica com a miniatura da foto
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    fotoAtualBase64 = bitmapParaBase64(bitmap);
                    if (!fotoAtualBase64.isEmpty()) {
                        cacheFotos.put(fotoAtualBase64, bitmap);
                    }
                    if (imgPreview != null) {
                        imgPreview.setImageBitmap(bitmap);
                        imgPreview.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    // Pede permissão de CAMERA em runtime
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
                final List<RegistoDiario> diario = (p != null) ? p.getDiario() : null;
                predescodificarFotos(diario);
                runOnUiThread(() -> mostrarRegistos(diario));
            } catch (Exception e) {
                runOnUiThread(() -> txtEstadoDiario.setText("Erro ao carregar diário: " + e.getMessage()));
            }
        }).start();
    }

    private void adicionarRegisto() {
        String texto = edtRegisto.getText().toString().trim();

        // Se não escreveu nada nem tirou foto, avisa
        if (texto.isEmpty() && fotoAtualBase64.isEmpty()) {
            Toast.makeText(this, "Escreve o estado de crescimento ou tira uma foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        // O model não deixa guardar texto vazio, por isso metemos um texto se for só foto
        if (texto.isEmpty()) {
            texto = "Foto adicionada";
        }

        final String textoFinal = texto;
        final String fotoParaEnviar = fotoAtualBase64;
        btnAdicionar.setEnabled(false);
        btnTirarFoto.setEnabled(false);

        new Thread(() -> {
            try {
                final Planta pAtualizada = PlantaService.adicionarRegistoDiario(plantaId, textoFinal, fotoParaEnviar);
                predescodificarFotos(pAtualizada.getDiario());
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

    // Pede confirmação antes de apagar um registo do diário
    private void confirmarApagarRegisto(RegistoDiario registo) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar registo")
                .setMessage("Tens a certeza que queres apagar este registo do diário?")
                .setPositiveButton("Apagar", (dialog, which) -> apagarRegisto(registo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Remove o registo no RestDB, limpa a foto da cache e atualiza a interface
    private void apagarRegisto(RegistoDiario registo) {
        txtEstadoDiario.setVisibility(View.VISIBLE);
        txtEstadoDiario.setText("A apagar registo...");

        new Thread(() -> {
            try {
                final Planta pAtualizada = PlantaService.removerRegistoDiario(plantaId, registo);
                String fotoApagada = registo.getFotoBase64();
                if (fotoApagada != null && !fotoApagada.isEmpty()) {
                    cacheFotos.remove(fotoApagada);
                }
                predescodificarFotos(pAtualizada.getDiario());
                runOnUiThread(() -> {
                    Toast.makeText(DiarioPlantaActivity.this, "Registo apagado do diário!", Toast.LENGTH_SHORT).show();
                    mostrarRegistos(pAtualizada.getDiario());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtEstadoDiario.setVisibility(View.GONE);
                    Toast.makeText(DiarioPlantaActivity.this, "Erro ao apagar registo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Descodifica as fotografias Base64 na thread de background antes de atualizar a UI
    private void predescodificarFotos(List<RegistoDiario> registos) {
        if (registos == null) return;
        for (int i = 0; i < registos.size(); i++) {
            RegistoDiario r = registos.get(i);
            if (r == null) {
                continue;
            }
            String base64 = r.getFotoBase64();
            if (base64 != null && !base64.isEmpty() && !cacheFotos.containsKey(base64)) {
                Bitmap bmp = base64ParaBitmap(base64);
                if (bmp != null) {
                    cacheFotos.put(base64, bmp);
                }
            }
        }
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
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(DiarioPlantaActivity.this)
                            .inflate(R.layout.item_diario, parent, false);
                }

                RegistoDiario r = getItem(position);
                TextView txtData = convertView.findViewById(R.id.txt_data_diario);
                TextView txtTexto = convertView.findViewById(R.id.txt_texto_diario);
                ImageView imgFoto = convertView.findViewById(R.id.img_registo_diario);
                ImageButton btnApagar = convertView.findViewById(R.id.btn_apagar_registo);

                if (r != null) {
                    txtData.setText(sdf.format(r.getDataHora()));
                    txtTexto.setText(r.getTexto());

                    String base64 = r.getFotoBase64();
                    Bitmap bmp = (base64 != null && !base64.isEmpty()) ? cacheFotos.get(base64) : null;
                    if (bmp != null) {
                        imgFoto.setImageBitmap(bmp);
                        imgFoto.setVisibility(View.VISIBLE);
                    } else {
                        imgFoto.setImageBitmap(null);
                        imgFoto.setVisibility(View.GONE);
                    }

                    btnApagar.setOnClickListener(v -> confirmarApagarRegisto(r));
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