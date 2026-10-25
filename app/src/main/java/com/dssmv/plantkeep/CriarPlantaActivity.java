package com.dssmv.plantkeep;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;

import com.dssmv.plantkeep.dto.EspecieOpb;
import com.dssmv.plantkeep.model.Planta;
import com.dssmv.plantkeep.services.PlantBookService;
import com.dssmv.plantkeep.services.PlantaService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecrã de criar planta.
 * Fluxo: pesquisar espécie no OpenPlantbook -> escolher a espécie -> dar um nome/aplido -> guardar.
 */
public class CriarPlantaActivity extends AppCompatActivity {

    private EditText edtPesquisa;
    private EditText edtNome;
    private Button btnPesquisar;
    private Button btnGuardar;
    private ListView lvResultados;
    private TextView lblResultado;
    private TextView txtEscolhida;

    private List<EspecieOpb> lista;   // resultados da pesquisa
    private EspecieOpb escolhida;     // espécie que o utilizador escolheu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtPesquisa = findViewById(R.id.edt_pesquisa);
        edtNome = findViewById(R.id.edt_nome);
        btnPesquisar = findViewById(R.id.btn_pesquisar);
        btnGuardar = findViewById(R.id.btn_guardar);
        lvResultados = findViewById(R.id.lv_resultados);
        lblResultado = findViewById(R.id.lbl_resultado);
        txtEscolhida = findViewById(R.id.txt_escolhida);

        btnPesquisar.setOnClickListener(v -> pesquisar());
        lvResultados.setOnItemClickListener((parent, view, position, id) -> escolher(position));
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void pesquisar() {
        final String termo = edtPesquisa.getText().toString().trim();
        if (termo.isEmpty()) {
            Toast.makeText(this, "Escreve o nome da espécie", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPesquisar.setEnabled(false);
        new Thread(() -> {
            try {
                final List<EspecieOpb> l = PlantBookService.pesquisar(termo);
                runOnUiThread(() -> mostrarResultados(l));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnPesquisar.setEnabled(true);
                    Toast.makeText(CriarPlantaActivity.this,
                            "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarResultados(List<EspecieOpb> l) {
        btnPesquisar.setEnabled(true);
        if (l == null || l.isEmpty()) {
            Toast.makeText(this, "Não encontrei nada com esse nome. Experimenta o nome científico (ex. lactuca).",
                    Toast.LENGTH_LONG).show();
            return;
        }
        lista = l;

        lvResultados.setAdapter(new ArrayAdapter<EspecieOpb>(this, 0, lista) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                EspecieOpb e = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(CriarPlantaActivity.this)
                            .inflate(R.layout.item_especie, parent, false);
                }
                TextView txt = convertView.findViewById(R.id.txt_especie);
                TextView txtComuns = convertView.findViewById(R.id.txt_comuns);
                ImageView img = convertView.findViewById(R.id.img_especie);
                txt.setText(textoEspecie(e));
                txtComuns.setText(joinComuns(e.getAliasesComuns()));

                final String url = e.getImageUrl();
                final ImageView imgRef = img;
                imgRef.setTag(url);
                if (url == null || url.isEmpty()) {
                    // sem imagem -> não mostra nada
                    imgRef.setVisibility(View.INVISIBLE);
                } else {
                    imgRef.setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        final Bitmap b = carregarImagem(url);
                        imgRef.post(() -> {
                            if (url.equals(imgRef.getTag()) && b != null) {
                                imgRef.setImageBitmap(b);
                            }
                        });
                    }).start();
                }
                return convertView;
            }
        });

        // passa de "pesquisa" para "resultados"
        edtPesquisa.setVisibility(View.GONE);
        btnPesquisar.setVisibility(View.GONE);
        lblResultado.setVisibility(View.VISIBLE);
        lvResultados.setVisibility(View.VISIBLE);
    }

    private void escolher(int position) {
        if (lista == null || position < 0 || position >= lista.size()) {
            return;
        }
        escolhida = lista.get(position);

        // passa de "resultados" para "escolha + nome/apelido"
        lblResultado.setVisibility(View.GONE);
        lvResultados.setVisibility(View.GONE);
        txtEscolhida.setText("Espécie: " + escolhida.getDisplayPid());
        txtEscolhida.setVisibility(View.VISIBLE);
        edtNome.setVisibility(View.VISIBLE);
        btnGuardar.setVisibility(View.VISIBLE);
    }

    private void guardar() {
        if (escolhida == null) {
            return;
        }
        final String nome = edtNome.getText().toString().trim();
        final String especie = escolhida.getDisplayPid();
        final String pid = escolhida.getPid();

        new Thread(() -> {
            try {
                Planta p = new Planta(nome, especie);
                p.setPid(pid);
                PlantaService.criar(p);
                runOnUiThread(() -> {
                    Toast.makeText(CriarPlantaActivity.this,
                            "Planta guardada!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(CriarPlantaActivity.this,
                        "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /** Texto mostrado para uma espécie: nome + alias entre parênteses. */
    private String textoEspecie(EspecieOpb e) {
        String texto = e.getDisplayPid() == null || e.getDisplayPid().isEmpty()
                ? e.getPid() : e.getDisplayPid();
        if (e.getAlias() != null && !e.getAlias().isEmpty()
                && !e.getAlias().equalsIgnoreCase(texto)) {
            texto = texto + " (" + e.getAlias() + ")";
        }
        return texto;
    }

    /** Junta os nomes comuns numa linha (ex. "Potato, Potato plant"). */
    private String joinComuns(List<String> comuns) {
        if (comuns == null || comuns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String c : comuns) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** Descarrega um bitmap a partir de um url (ou null se falhar). Roda fora da UI. */
    private static Bitmap carregarImagem(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            InputStream in = c.getInputStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            return null;
        }
    }
}
